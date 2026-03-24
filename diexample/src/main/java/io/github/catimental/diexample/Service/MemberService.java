package io.github.catimental.diexample.Service;

import org.springframework.stereotype.Service;


import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.exception.ErrorCode;
import io.github.catimental.diexample.security.JwtProvider;
import org.springframework.transaction.annotation.Transactional;
import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.DTO.MemberResponse;
import io.github.catimental.diexample.DTO.MemberLoginRequest;
import io.github.catimental.diexample.DTO.MemberRegisterRequest;
import io.github.catimental.diexample.exception.ApiException;
// import io.github.catimental.diexample.config.SecurityBeansConfig;



import org.springframework.security.crypto.password.PasswordEncoder;
import io.github.catimental.diexample.DTO.refreshToken.TokenPairResponse;

import io.github.catimental.diexample.DTO.MemberUpdateRequest;


@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    
    private final JwtProvider jwtProvider;

    private final RefreshTokenService refreshTokenService;


    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, 
          JwtProvider jwtProvider, RefreshTokenService refreshTokenService){
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
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
        member.setCreatedAt();

        
        Member saved = memberRepository.save(member);

        return new MemberResponse(saved.getId(), saved.getLoginId(), saved.getRoleName());
    }



    public TokenPairResponse login(MemberLoginRequest req){
        Member member = memberRepository.findByLoginId(req.loginId())
                        .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "id no exist"));
        
        if(!passwordEncoder.matches(req.password(), member.getLoginPassword())){
            throw new ApiException(ErrorCode.INVALID_PASSWORD, "incorrect password");
        }

        

        String access = jwtProvider.createAccessToken(member.getId(), member.getRoleName());
        String refresh = refreshTokenService.issueAndStoreRefresh(member.getId());

        return new TokenPairResponse(access, refresh);

        //return new MemberResponse(member.getId(), member.getLoginId(), member.getRoleName());

    }

    



    public void updateMember(Long memberId, MemberUpdateRequest req){
            Member member = memberRepository.findByLoginId(req.loginId())
                                        .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "Member does not exist"));

          
                member.changeLoginId(req.loginId());
                member.changeLoginPassword(passwordEncoder.encode(req.loginPassword()));
    }


    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId){
        Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND, "no member exists"));
        
        return new MemberResponse(member.getId(), member.getLoginId(), member.getRoleName());
    }
}
