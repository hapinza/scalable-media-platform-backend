package io.github.catimental.diexample.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.catimental.diexample.DTO.progress.ProgressItemResponse;
import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.Repository.WatchingProgressRepository;
import io.github.catimental.diexample.exception.ErrorCode;
import io.github.catimental.diexample.exception.ApiException;
import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.domain.progress.WatchingProgress;

import java.util.List;


@Service
@Transactional
public class WatchingProgressService {
    
    private final WatchingProgressRepository progressRepository;
    private final MemberRepository memberRepository;


    public WatchingProgressRepository(WatchingProgressRepository progressRepository, MemberRepository memberRepository){
        this.progressRepository = progressRepository;
        this.memberRepository = memberRepository;
    }

    public void upsert(Long memberId, Long movieId, Integer progressSeconds){
        if(progressSeconds == null || progressSeconds < 0){
            throw new ApiException(ErrorCode.INVALID_PROGRESS, "prograssseconds should be more than 0");
        }

        var existing = progressRepository.findByMemberIdAndMovieId(memberId, movieId)

        
        if(existing.isPresent()){
            existing.get().updateProgress(progressSeconds);
            return ;
        }

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "There is no user"));


        
        progressRepository.save(new WatchingProgress(memberId, movieId, progressSeconds));

    }


    @Transactional(readOnly = true)
    public List<ProgressItemResponse> list(Long memberId){
        return progressRepository.findAllByMemberIdOrderByUpdatedAtDesc(memberId)
                    .stream()
                    .map(p -> new ProgressItemResponse(p.getMovieId(), p.getProgressSeconds(), p.getUpdatedAt()))
                    .toList();
    }


}


    public void remove(Long memberId, Long movieId){
        WatchingProgress p = progressRepository.findByMemberIdAndMovieId(memberId, movieId)
                                .orElseThrow(() -> new ApiException(ErrorCode.PROGRESS_NOT_FOUND, "You have not watched this content"));
        
        progressRepository.delete(p);
    }