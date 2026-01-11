package io.github.catimental.diexample.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    public Member(){};

    


}
