package io.github.catimental.diexample.controller;

import io.github.catimental.diexample.Service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import io.github.catimental.diexample.DTO.MemberLoginRequest;
import io.github.catimental.diexample.DTO.MemberRegisterRequest;
import io.github.catimental.diexample.DTO.MemberResponse;
import io.github.catimental.diexample.DTO.refreshToken.TokenPairResponse;

import org.springframework.security.core.Authentication;

// member connection
import io.github.catimental.diexample.domain.Member;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberRestController {

    private final MemberService memberService;

    @Autowired
    public MemberRestController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@RequestBody MemberRegisterRequest req){
        return ResponseEntity.ok(memberService.register(req));
    }


    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@ResquestBody MemberLoginRequest req){
        return ResponseEntity.ok(memberService.login(req));
    }

    @PutMapping("/modify")
    public ResponseEntity<MemberResponse> updateMember(@RequestBody MemberUpdateRequest req, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(memberService.updateMember(memberId , req));

    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.of(memberService.getMe(memberId));
    }
















    // @PostMapping("/register")
    // public Map<String, Object> register(@RequestBody Member member){
    //     Long id = memberService.register(member);
    //     return Map.of(
    //             "id", id,
    //             "message", "successfully sign-in"
    //     );
    // }

    // @PostMapping("/login")
    // public Map<String, Object> login(@RequestBody Member form){
    //     var optionalMember = memberService.login(form.getLoginId(), form.getLoginPassword());

    //     if(optionalMember.isPresent()){
    //         return Map.of(
    //                 "success", true,
    //                 "memberId", optionalMember.get().getId()
    //         );
    //     } else {
    //         return Map.of(
    //                 "success", false,
    //                 "message", "sign in fail"
    //         );
    //     }
    // }







// //login
// @PostMapping("/login")
// // MemberResponse = ResponseEntity.data (JSON)
// public ResponseEntity<MemberResponse> login(@RequestBody MemberLoginReqeust req){
//     // could exist or not optional<T>, type generic 
//     var optionalMember  = memberService.login(req.getLoginId(), req.getLoginPassword());

//     if(optionalMember.isPresent()){
//         return ResponseEntity.ok(
//             new MemberResponse(true, optionalMember.get().getId(), "log in success")
//         );
//     }else{
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//         .body(new MemberResponse(false, null, "log in fail"));
//     }
// }

// // register
// @PostMapping("/register")
// public ResponseEntity<MemberResponse> register(@RequestBody MemberRegisterRequest req){
//     Long id = memberService.register(req);


//     // public static <T> ResponseEntity<T> ok(T body)
//     return RequestEntity.ok(
//         new MemberResponse(true, id, "sign in success")
//     );
// }

// // search the user
// // ResponseEntity<Member> because it has to show the user info.
// @GetMapping("/{id}")
// public ResponseEntity<Member> getMember(@PathVariable Long id){
//     var member = memberService.findMember(id);

//     // from Optional<Member> to ReseponseEntity with ok(Member)  , mapping
//     return member.map(ResponseEntity::ok)
//             .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
// }



// // modify the User
// // 1. check if there is an actual user
// // 2. if it exists, change the User info
// // ResponseEntity<MemberResponse> because of it shows whether it is passing or not
// // not Json data
// @PutMapping("/{id}")
// public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id, @RequestBody MemberUpdateRequest req){
//     Member member = memberSerivce.updateMember(id, req);
//     return ResponseEntity.ok(new MemberResponse(true, member.getId(), "successfully modified")
// );

// }


// @DeleteMapping("/{id}")
// public ReponseEntity<MemberResponse> deleteMember(@PathVariable Long id){
//     memberService.deleteMember(id);
//     return ResponseEntity.ok(new MemberReponse(true, id, "successfully deleted")
// );

// }


}

