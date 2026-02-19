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


import io.github.catimental.diexample.exception.ErrorCode;

@Service
@Transactional
public class MovieLikeService {

  
    private final MovieLikeRepository movieLikeRepository;
    private final MemberRepository memberRepository;

    public MovieLikeService(MovieLikeRepository movieLikeRepository, MemberRepository memberRepository, AnalyticsController analyticsController){
        this.movieLikeRepository = movieLikeRepository;
        this.memberRepository = memberRepository;
    }


    public void upsert(Long memberId, Long movieId, boolean like){
        var existing = movieLikeRepository.findByMemberIdAndMovieId(memberId, movieId);

        if(existing.isPresent()){
            existing.get().update(like);
            return ;
        }

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "There is no user"));


        
        movieLikeRepository.save(new MovieLike(member, movieId, like));

    }


    public void remove(Long memberId, Long movieId){
        MovieLike ml = movieLikeRepository.findByMemberIdAndMovieId(memberId, movieId)
                        .orElseThrow(() -> new ApiException(ErrorCode.LIKE_NOT_FOUND, "There is not like"));

        movieLikeRepository.delete(ml);
    }


    @Transactional(readOnly = true)
    public Page<MovieLikeItemResponse> likeLists(Long memberId, Pageable pageable){
        return movieLikeRepository.findAllByMemebrIdAndLikeTrueOrderByUpdatedAtDesc(memberId, pageable)
                            .map(p -> new MovieLikeItemResponse(p.getMovieId(), p.isLike(), p.getUpdatedAt()));
    } 







}
