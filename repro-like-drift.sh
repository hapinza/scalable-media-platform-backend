#!/usr/bin/env bash
# Reproduces the like-count drift between MySQL and the Redis read model.
#
# Usage (from the repo root, after `docker compose up -d --build`):
#   bash repro-like-drift.sh
#   MOVIE=42 bash repro-like-drift.sh      # use a different movie id
#
# Expected before the fix:
#   after 10 identical likes -> MySQL liked rows: 1 | Redis likeCount: 10
#   after deleting the like  -> MySQL liked rows: 0 | Redis likeCount: 10
# Expected after the fix:
#   after 10 identical likes -> MySQL liked rows: 1 | Redis likeCount: 1
#   after deleting the like  -> MySQL liked rows: 0 | Redis likeCount: 0

set -euo pipefail

BASE=${BASE:-http://localhost:8080}
MOVIE=${MOVIE:-9001}
WAIT=${WAIT:-5}   # seconds to let the Outbox poller and consumer catch up
LOGIN="repro_$(date +%s)"

# Register a fresh member so earlier runs do not affect the result.
resp=$(curl -s -X POST "$BASE/api/members/register" \
  -H 'Content-Type: application/json' \
  -d "{\"loginId\":\"$LOGIN\",\"password\":\"Repro1234!\"}")
MEMBER=$(echo "$resp" | sed -nE 's/.*"id":([0-9]+).*/\1/p')
if [ -z "$MEMBER" ]; then
  echo "Register failed: $resp"
  exit 1
fi
echo "member=$MEMBER movie=$MOVIE"

counts() {
  local redis mysql
  redis=$(docker compose exec -T redis redis-cli HGET "movie:$MOVIE:stats" likeCount | tr -d '\r')
  mysql=$(docker compose exec -T db mysql -uroot -pdltjdgh1234 -N -s mydb \
    -e "SELECT COUNT(*) FROM movie_like WHERE movie_id=$MOVIE AND is_like=1" 2>/dev/null | tr -d '\r')
  echo "  MySQL liked rows: $mysql | Redis likeCount: ${redis:-<empty>}"
}

echo "Before:"
counts

echo "1) Send the same like request 10 times"
for _ in $(seq 1 10); do
  curl -s -o /dev/null -w "%{http_code} " -X PUT "$BASE/test/likes/$MOVIE?memberId=$MEMBER" \
    -H 'Content-Type: application/json' -d '{"like":true}'
done
echo
sleep "$WAIT"
counts

echo "2) Delete the like"
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$BASE/test/likes/$MOVIE?memberId=$MEMBER"
sleep "$WAIT"
counts
