package com.example.demo.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 스프링 시큐리티 주용 아키텍처 이해
 * 2. 필터 초기화와 다중 보안 설정
 */

//@EnableWebSecurity
public class SecurityConfig1 {

    @Bean
    @Order(0)
    public SecurityFilterChain filterChain1(@NotNull HttpSecurity http) throws Exception {
        /* /admin 으로 시작되는 모든 url 에 대해서만 인증 체크
        *  /admin 이외의 url 에서는 인증 체크 하지 않음
        * */
        http
                .antMatcher("/admin/**")
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic();

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain2(@NotNull HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .formLogin();

        return http.build();
    }

}
