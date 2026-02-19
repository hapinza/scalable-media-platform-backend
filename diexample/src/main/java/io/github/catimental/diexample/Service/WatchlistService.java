package io.github.catimental.diexample.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.Repository.WatchlistRepository;
import io.github.catimental.diexample.exception.*;
import io.github.catimental.diexample.domain.watchlist.Watchlist;
import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.DTO.*;

import java.util.List;


@Service
public class WatchlistService {
    
    private final WatchlistRepository watchlistRepository;
    private final MemberRepository memberRepository;


    public WatchlistService(WatchlistRepository watchlistRepository , MemberRepository memberRepository){
        this.watchlistRepository = watchlistRepository;
        this.memberRepository = memberRepository;
    }
    


    public void add(Long memberId, Long movieId){
        if(watchlistRepository.existsByMemberIdAndMovieId(memberId, movieId)){
            throw new ApiException(ErrorCode.ALREADY_WACHLISTED, "already have on watchlist");
        }
           
        // event though we already passed the member with authentication
        // since jwt and db are different in update, it will possibly cause an error.
        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "Member does not exist"));


        watchlistRepository.save(new Watchlist(member, movieId));
    }


    public void remove(Long memberId, Long movieId){
        Watchlist w = watchlistRepository.findByMemberIdAndMovieId(memberId, movieId)
                        .orElseThrow(() -> new ApiException(ErrorCode.WATCHLIST_NOT_FOUND, "there is no watchlist"));

        watchlistRepository.delete(w);
    }


    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> list(Long memberId){
        // return watchlistRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId)
        //                             .stream() // stream list
        //                             .map(w -> new WatchlistItemResponse(w.getMovieId(), w.getCreatedAt()))
        //                             .toList();
        
        return watchlistRepository.findItem(memberId);
    }


}
