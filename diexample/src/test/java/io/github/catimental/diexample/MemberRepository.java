package io.github.catimental.diexample;

import org.springframework.stereotype.Repository;


@Repository 
public class MemberRepository {
    public String findMember(){
        return "Memeber: Seongho";
    }


}
