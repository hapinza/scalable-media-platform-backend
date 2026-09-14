package io.github.catimental.diexample.Service.Like;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.catimental.diexample.DTO.Like.MovieLikeItemResponse;
import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.controller.views.AnalyticsController;
import io.github.catimental.diexample.Repository.Like.MovieLikeRepository;
import io.github.catimental.diexample.exception.ApiException;
import io.github.catimental.diexample.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.github.catimental.diexample.domain.like.MovieLike;
import io.github.catimental.diexample.Repository.outbox.OutboxRepository;

import java.util.Map;
import java.util.HashMap;
import io.github.catimental.diexample.exception.ErrorCode;
import io.github.catimental.diexample.domain.event.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;



@Service
@Transactional
public class MovieLikeService {

  
    private final MovieLikeRepository movieLikeRepository;
    private final MemberRepository memberRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public MovieLikeService(MovieLikeRepository movieLikeRepository, MemberRepository memberRepository, 
        OutboxRepository outboxRepository,
        ObjectMapper objectMapper){
        this.movieLikeRepository = movieLikeRepository;
        this.memberRepository = memberRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }


    public void upsert(Long memberId, Long movieId, boolean like){
        memberRepository.findById(memberId).
                            orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "no user"));

        movieLikeRepository.upsertLike(memberId, movieId, like);

        createOutbox(memberId, movieId, like);

        // Use atomic upsert to avoid race conditions from check-then-insert/update logic under concurrency

        
    }


    private void createOutbox(Long memberId, Long movieId, boolean like){
        try{
            int delta = like ? 1 : -1;
            String eventType = like ? "LIKE_CREATED" : "LIKE_REMOVED";



            Map<String, Object> payload = new HashMap<>();
            payload.put("memberId", memberId);
            payload.put("movieId", movieId);
            payload.put("type", "LIKE_CREATED");
            payload.put("delta", delta);

            /*
            EventType:
            
            Redis Stream
  ├─ LIKE_CREATED      → LikeAggregationConsumer
  ├─ LIKE_REMOVED      → LikeAggregationConsumer
  ├─ VIEW_CREATED      → ViewAggregationConsumer
  ├─ RATING_UPDATED    → RatingAggregationConsumer
  └─ COMMENT_CREATED   → CommentAggregationConsumer
            */
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent(
                "LIKE",
                memberId + ":" + movieId,
                eventType,
                payloadJson
            );

            outboxRepository.save(outboxEvent);

        }catch(Exception e){
            throw new RuntimeException("Failed to create outbox event", e);
        }


    }


    public void remove(Long memberId, Long movieId){
        MovieLike ml = movieLikeRepository.findByMemberIdAndMovieId(memberId, movieId)
                        .orElseThrow(() -> new ApiException(ErrorCode.LIKE_NOT_FOUND, "There is not like"));

        movieLikeRepository.delete(ml);
    }


    @Transactional(readOnly = true)
    public Page<MovieLikeItemResponse> likeLists(Long memberId, Pageable pageable){
        return movieLikeRepository.findAllByMember_IdAndLikeTrueOrderByUpdatedAtDesc(memberId, pageable)
                            .map(p -> new MovieLikeItemResponse(p.getMovieId(), p.isLike(), p.getUpdatedAt()));
    } 







}
