package com.everyplaceinkorea.epik_boot3_api.member.mypage.Info.controller;


import com.everyplaceinkorea.epik_boot3_api.anonymous.signup.service.SignupService;
import com.everyplaceinkorea.epik_boot3_api.auth.dto.AuthRequestDto;
import com.everyplaceinkorea.epik_boot3_api.auth.dto.AuthResponseDto;
import com.everyplaceinkorea.epik_boot3_api.auth.dto.GoogleRequestDto;
import com.everyplaceinkorea.epik_boot3_api.auth.dto.GoogleResponseDto;
import com.everyplaceinkorea.epik_boot3_api.auth.entity.EpikUserDetails;
import com.everyplaceinkorea.epik_boot3_api.auth.util.JwtUtil;
import com.everyplaceinkorea.epik_boot3_api.entity.member.Member;
import com.everyplaceinkorea.epik_boot3_api.member.mypage.Info.dto.InfoRequestDto;
import com.everyplaceinkorea.epik_boot3_api.member.mypage.Info.service.DefaultInfoService;
import com.everyplaceinkorea.epik_boot3_api.member.mypage.Info.service.InfoService;
import com.everyplaceinkorea.epik_boot3_api.repository.Member.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;


@RestController
@RequestMapping("mypage")
public class InfoController {

    @Autowired
    public InfoService infoService;

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    @Autowired
    private MemberRepository memberRepository;

    public InfoController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/info")
    public ResponseEntity<?> update(@RequestBody InfoRequestDto infoRequestDto, HttpServletResponse response, HttpServletRequest request) {

        Long Id = infoRequestDto.getId();
        System.out.println("업데이트할 멤버 id-"+Id);

        try {
            
            EpikUserDetails userDetails =(EpikUserDetails) infoService.updateInfo(infoRequestDto);
            System.out.println(userDetails);

            String token = jwtUtil.generateToken(userDetails); // 사용자 정보 가져와 토튼 만들기
            System.out.println("Generated Token: " + token); // JWT 토큰 출력

            // 토큰을 쿠키에 저장
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(true); // JavaScript에서 접근할 수 없도록 설정
            cookie.setSecure(true);   // HTTPS에서만 전송하도록 설정
            cookie.setPath("/");      // 쿠키가 모든 경로에서 유효하도록 설정
            cookie.setMaxAge(60 * 60 * 24); // 1일 동안 쿠키 유지
            // 쿠키를 응답에 추가
            response.addCookie(cookie);

            // JWT 토큰 반환
            AuthResponseDto responseDto = AuthResponseDto
                    .builder()
                    .memberId(userDetails.getId())
                    .token(token) // 여기에 인증 상태 정보를 모두 담아서 전달함
                    .build();

            System.out.println("변경된 토큰-"+token);

                return ResponseEntity.ok(responseDto);

        }
        catch (AuthenticationException e) {
            System.out.println("예외이유 " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid info update");
        }
    }

}
