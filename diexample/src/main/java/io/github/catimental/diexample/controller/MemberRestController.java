package io.github.catimental.diexample.controller;

import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map



@RestController
@RequestMapping
@CrossOrigin(origins = "*")
public class MemberRestController {
    private final MemberService memberservice;

    @Autowired
    public MemberRestController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/register")
    


}
