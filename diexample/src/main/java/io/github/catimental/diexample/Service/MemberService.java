package io.github.catimental.diexample.Service;

import org.springframework.stereotype.Service;
import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.exception.ErrorCode;
import io.github.catimental.diexample.security.JwtProvider;
import jakarta.transaction.Transactional;
import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.DTO.MemberResponse;
import io.github.catimental.diexample.DTO.MemberLoginRequest;
import io.github.catimental.diexample.DTO.MemberRegisterRequest;
import io.github.catimental.diexample.exception.ApiException;
// import io.github.catimental.diexample.config.SecurityBeansConfig;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import io.github.catimental.diexample.security.*;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    
    private final JwtProvider jwtProvider;


    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, 
          JwtProvider jwtProvider){
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }


    // we don't return the optioanl<T> to get controller to handle the exception
    // which is not its task
    public MemberResponse register(MemberRegisterRequest req){
        if(memberRepository.existsByLoginId(req.loginId())){
            throw new ApiException(ErrorCode.DUPLICATE_LOGIN_ID, "already exist");
        }

        Member member = new Member();
        member.setLoginId(req.loginId());
        member.setLoginPassword(passwordEncoder.encode(req.password()));
        member.assignUserRole();
        member.setCreatedAt(LocalDateTime.now());

        
        Member saved = memberRepository.save(member);

        return new MemberResponse(saved.getId(), saved.getLoginId(), saved.getRoleName());
    }



    public MemberResponse login(MemberLoginRequest req){
        Member member = memberRepository.findByLoginId(req.loginid())
                        .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "id no exist"));
        
        if(!passwordEncoder.mathches(req.password(), member.getLoginPassword())){
            throw new ApiException(ErrorCode.INVALID_PASSWORD, "incorrect password");
        }


        String token = jwtProvider.createAccessToken(member.getId(), member.getRole().name());

        return new MemberResponse(member.getId(), member.getLoginId(), member.getRoleName());

    }




    public MemberResponse updateMember(MemberUpdateRequest req){
        

    }


    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId){
        Member member = memberRepository.findByLoginId(memberId)
                    .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "no member exists"));
        
        return new MemberResponse(member.getId(), member.getLoginId(), member.getRoleName());
    }
}
