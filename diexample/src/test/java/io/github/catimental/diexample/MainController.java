package io.github.catimental.diexample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {
    private final MemberService memberService;

    @Autowired
    public MainController(MemberService memberService){
        this.memberService = memberService;
    }

    public void showMember(){
        System.out.println(memberService.getMemberName());
    }

}
