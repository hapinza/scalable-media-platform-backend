package io.github.catimental.diexample.Service.Rating;
import org.springframework.transaction.annotation.Transactional;


import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.Repository.Rating.MovieRatingRepository;
import io.github.catimental.diexample.domain.rating.MovieRating;
import io.github.catimental.diexample.exception.ApiException;
import io.github.catimental.diexample.domain.Member;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.github.catimental.diexample.DTO.rating.RatingUpsertResponse;

import io.github.catimental.diexample.exception.ErrorCode;


@Transactional
@Service
public class MovieRatingService {
    private final MovieRatingRepository movieRatingRepository;
    private final MemberRepository memberRepository;


    public MovieRatingService(MovieRatingRepository movieRatingRepository, MemberRepository memberRepository)
    {
        this.movieRatingRepository = movieRatingRepository;
        this.memberRepository = memberRepository;
    }


    public void upsert(Long memberId, Long movieId, int score){
        if(score < 1 || score > 10) {
            throw new ApiException(ErrorCode.INVALID_RATING, "score should be between 1 to 10");
        }

        var existing = movieRatingRepository.findByMemberIdAndMovieId(memberId, movieId);

        if(existing.isPresent()){
            existing.get().updateScore(score);
            return ;
        }

        Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "There is no user"));

        
        movieRatingRepository.save(new MovieRating(member, movieId, score));


    }


    public void remove(Long memberId, Long movieId){
        MovieRating mr = movieRatingRepository.findByMemberIdAndMovieId(memberId, movieId)
                            .orElseThrow(() -> new ApiException(ErrorCode.RATING_NOT_FOUND, "There is no rating record"));
        
        movieRatingRepository.delete(mr);
    }


    @Transactional(readOnly = true)
    public Page<RatingUpsertResponse> list(Long memberId, Pageable pageable){
        
        return movieRatingRepository.findAllByMemberIdOrderByUpdatedAtDesc(memberId, pageable)
                    .map(p -> new RatingUpsertResponse(p.getMovieId(), p.getScore(), p.getUpdatedAt()));

    }


}
