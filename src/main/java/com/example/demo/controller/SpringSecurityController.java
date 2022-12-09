package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@Slf4j
public class SpringSecurityController {

    @GetMapping("/")
    public String main(HttpSession session) {
        // Authentication 객체 조회1
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Authentication 객체 조회2
        SecurityContext context = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        Authentication authentication1 = context.getAuthentication();
        return "home";
    }

    @GetMapping("/thread")
    public String thread() {
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    // SecurityContextHolder 의 default 값이 'MODE_THREADLOCAL' 이기 때문에
                    // 자식 쓰레드에서는 Authentication 정보를 조회할 수 없다.
                    // 하지만 SecurityConfig2.java 파일에서 strategy 설정값 변경을 통해 자식 쓰레드에서도 Authentication 정보를 조회할 수 있도록 수정
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                }
            }
        ).start();
        return "thread";
    }

    @GetMapping("/user")
    public String user() {
        return "user";
    }

    @GetMapping("/admin/pay")
    public String adminPay() {
        return "adminPay";
    }

    @GetMapping("/admin/**")
    public String admin() {
        return "admin";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/denied")
    public String denied() {
        return "Access id denied";
    }

}
