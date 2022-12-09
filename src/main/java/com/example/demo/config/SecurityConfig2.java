package com.example.demo.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 스프링 시큐리티 주용 아키텍처 이해
 * 4. 인증 저장소 - SecurityContextHolder, SecurityContext
 */

@EnableWebSecurity
public class SecurityConfig2 {

    @Bean
    public SecurityFilterChain filterChain(@NotNull HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin();

        // '/thread' 로 호출 시 자식 쓰레드에서도 Authentication 객체가 조회 되도록 설정
        // default 값은 'MODE_THREADLOCAL' 임
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        return http.build();
    }

}