package io.github.catimental.diexample.DTO;



public record LoginResponse(
    Long memberId,
    String loginId,
    String role, 
    String accessToken
){}
