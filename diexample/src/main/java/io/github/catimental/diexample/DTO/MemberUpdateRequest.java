package io.github.catimental.diexample.DTO;

public record MemberUpdateRequest (
    String loginId,
    String loginPassword
){}
