package com.everyplaceinkorea.epik_boot3_api.anonymous.signup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponsetDto {

    private String username;
    private String nickname;
    private String password;
    private String email;
    private String profileImg;
    private String profileText;

    private LocalDate joinDate;
    private LocalDateTime lastAccess;
    //회원분류 - 정상회원1, 탈퇴회원2, 강퇴회원3
    private Byte type;
    //아이디1, 네이버2, 카카오3, 구글4
    private String loginType;
    //회원권한 - 회원1, 관리자2
    private String role;
}
