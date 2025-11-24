package io.github.catimental.diexample.controller;

import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map



@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MemberRestController {
    private final MemberService memberservice;

    @Autowired
    public MemberRestController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Member memebr){
        Long id = memberService.register(member);
        return Map.of(
          "id", id,
          "message", "successfully sign-in"
        );

    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Member form){
       
       //Optional<Member>
        var optionalMember = memberService.login(form.getLoginId(), form.getLoginPassword());

       if(optionalMember.isPresent()){
        return Map.of(
            "success", true, 
            "memeberId", optionalMember.get().getId();
        )
       }else{
        return Map.of(
            "success", false,
            "message" , "sign in fail");
       }
    }
        

    }


}
