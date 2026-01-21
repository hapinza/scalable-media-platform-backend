package io.github.catimental.diexample.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.github.catimental.diexample.DTO.LoginResponse;
import io.github.catimental.diexample.DTO.MemberRegisterRequest;
import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.Service.MemberService;
import io.github.catimental.diexample.domain.Member;

import  static org.mockito.Mockito.*;
import  static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MemberServiceTest {
    
    @Autowired
    private MemberService memberService;


    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp(){

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }


    public void testRegister(){
        MemberRegisterRequest req = new MemberRegisterRequest("testuser", "password123");
        Member mockMember = new Member();

        mockMember.setLoginId("testUser");
        mockMember.setPassword("encodedPassword");

        when(memberRepository.existsByLoginId("testuser")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(mockMember);

        MemberResponse response = memberService.register(req);

        assertNotNull(response);
        assertEquals("testuser", req.loginId());
        assertNotEquals("password123", mockMember.getLoginPassword());

    }



    @Test
    public void testLogin(){
        MemberLoginRequest req = new MemberLoginReqeust("testuser", "password123");
        Member mockMember = new Member();

        mockMember.setLoginId("testuser");
        mockMember.setLoginPassword("encodedPassword");

        when(memberRepository.findByLoginId("testuser")).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);


        LoginResponse response = memberService.login(req);

        assertNotNull(response);
        assertEquals("testuser", response.loginId());
        assertNotNull(response.accessToken());
        assertTrue(response.accessToken().startsWith("Bearer "));
    }

    




}
