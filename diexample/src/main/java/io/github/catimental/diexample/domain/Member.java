package io.github.catimental.diexample.domain;


@Entity
public class Member {
    @Id 
    @GneeratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // No allow null in DB, only one unique column 
    @Column(nullable = false, unique = true)
    private String loginId;
    

    @Column(nullable = false)
    private String loginPassword;


    // fixed role to prevent changing the role 
    // to compare the value with ==
    // don't make the variable "user" in the DTO
    // instead, separate the two part with admin service, controller and user ""
    @Enumerated(EnumType.STRING)
    private Role role;



    @Column(nullable = false)
    private LocalDateTime createdAt;


    public Member() {}


    public Member(String loginId, String loginPassword){
        this.loginId = loginId;
        this.loginPassword = loginPassword;
    }

    public Long getId(){
        return this.Id;
    }

    public void setId(Long id){
        this.id = id;
    }


    //getter
    public String getLoginId(){
        return this.loginId;
    }
    //getter
    public String getLoginPassword(){
        return this.loginPassword;
    }
    //setter
    public void setLoginId(String loginId){
        this.loginId = loginId;
    }

    //setter 
    public void setLoginPassword(String loginPassword){
        this.loginPassword = loginPassword;
    }

    
    // set the role when registering in service
    // when it comes to role, make the setter but
    // don't just set the role to avoid exposure

    //role setter
    public void assignUserRole(){
        this.role = Role.USER;
    }

    public void assignAdmin(){
        this.role = Role.ADMIN;
    }


    public String getRoleName(){
        return role.name();
    }

}
