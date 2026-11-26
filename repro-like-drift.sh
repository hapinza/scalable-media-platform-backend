#!/usr/bin/env bash
# Checks that like events keep the Redis read model in step with MySQL.
#
# Usage (from the repo root, after `docker compose up -d --build`):
#   bash repro-like-drift.sh
#   MOVIE=42 bash repro-like-drift.sh      # use a different movie id
#
# The script records the movie's counts before it starts and checks changes
# relative to that baseline, so movies that already have likes work too.
#
# Expected (relative to baseline):
#   after 10 identical likes -> MySQL +1 | Redis +1
#   after deleting the like  -> MySQL +0 | Redis +0
# Before the fix this showed Redis +10 after the likes and +10 after the delete.

set -euo pipefail

BASE=${BASE:-http://localhost:8080}
MOVIE=${MOVIE:-9001}
WAIT=${WAIT:-5}   # seconds to let the Outbox poller and consumer catch up
LOGIN="repro_$(date +%s)"

echo "Waiting for the app at $BASE ..."
for i in $(seq 1 30); do
  if curl -s -o /dev/null "$BASE/api/members/register"; then break; fi
  if [ "$i" -eq 30 ]; then
    echo "App is not reachable at $BASE. Check: docker compose ps / docker compose logs app --tail 50"
    exit 1
  fi
  sleep 2
done

# Register a fresh member so this member has no earlier like on the movie.
resp=$(curl -sS -X POST "$BASE/api/members/register" \
  -H 'Content-Type: application/json' \
  -d "{\"loginId\":\"$LOGIN\",\"password\":\"Repro1234!\"}")
MEMBER=$(echo "$resp" | sed -nE 's/.*"id":([0-9]+).*/\1/p')
if [ -z "$MEMBER" ]; then
  echo "Register failed: $resp"
  exit 1
fi
echo "member=$MEMBER movie=$MOVIE"

mysql_count() {
  docker compose exec -T db mysql -uroot -pdltjdgh1234 -N -s mydb \
    -e "SELECT COUNT(*) FROM movie_like WHERE movie_id=$MOVIE AND is_like=1" 2>/dev/null | tr -d '\r'
}
redis_count() {
  local v
  v=$(docker compose exec -T redis redis-cli HGET "movie:$MOVIE:stats" likeCount 2>/dev/null | tr -d '\r')
  echo "${v:-0}"   # a missing key is treated as 0
}

FAIL=0
check() {  # label, expected mysql delta, expected redis delta
  local m r dm dr
  m=$(mysql_count); r=$(redis_count)
  dm=$((m - M0)); dr=$((r - R0))
  printf '  %-28s MySQL %s (%+d) | Redis %s (%+d)' "$1" "$m" "$dm" "$r" "$dr"
  if [ "$dm" -eq "$2" ] && [ "$dr" -eq "$3" ]; then
    echo "  OK"
  else
    printf '  FAIL (expected %+d / %+d)\n' "$2" "$3"
    FAIL=1
  fi
}

# Let events from earlier activity drain before reading the baseline. This is a
# bounded wait, so it narrows the window but does not guarantee a drained
# pipeline; run the script while nothing else is liking this movie.
sleep "$WAIT"
M0=$(mysql_count); R0=$(redis_count)
echo "Baseline: MySQL liked rows $M0 | Redis likeCount $R0"
if [ "$M0" -ne "$R0" ]; then
  echo "  Note: baseline already differs by $((R0 - M0)) (drift from earlier runs or a missing Redis key)."
  echo "        The checks below compare changes only, so they stay valid as long as"
  echo "        no other like events for this movie are in flight during the run."
fi

echo "1) Send the same like request 10 times"
for _ in $(seq 1 10); do
  curl -s -o /dev/null -w "%{http_code} " -X PUT "$BASE/test/likes/$MOVIE?memberId=$MEMBER" \
    -H 'Content-Type: application/json' -d '{"like":true}'
done
echo
sleep "$WAIT"
check "after 10 identical likes" 1 1

echo "2) Delete the like"
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$BASE/test/likes/$MOVIE?memberId=$MEMBER"
sleep "$WAIT"
check "after delete" 0 0

exit "$FAIL"
