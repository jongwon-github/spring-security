package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}1111")
                .roles("USER")
                .build();

        UserDetails sys = User.builder()
                .username("sys")
                .password("{noop}1111")
                .roles("SYS")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}1111")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, sys, admin);
    }

    @Bean
    public SecurityFilterChain filterChain(@NotNull HttpSecurity http) throws Exception {
        http
            // 인증 및 인가
            .authorizeRequests()
            .antMatchers("/user").hasRole("USER")
            .antMatchers("/admin/pay").hasRole("ADMIN")
            .antMatchers("/admin/**").access("hasRole('ADMIN') or hasRole('SYS')")
            .anyRequest()
            .authenticated();

        // login
        http
            .formLogin()
            //.loginPage("/loginPage") 직접 구현한 login 페이지 설정을 위한 설정, 이 부분 생략하면 spring security 에서 제공하는 로그인  페이지로 이동
            .defaultSuccessUrl("/") // 로그인 성공 후 이동 페이지
            .usernameParameter("username") // 아이디 파라미터명 설정
            .passwordParameter("password") // 패스워드 파라미터명 설정
            .loginProcessingUrl("/login")
            .successHandler(new AuthenticationSuccessHandler() {
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                    log.info("로그인 성공");
                    String redirectUrl = "/";
                    RequestCache requestCache = new HttpSessionRequestCache();
                    SavedRequest savedRequest = requestCache.getRequest(request, response);
                    if (savedRequest != null) {
                        redirectUrl = savedRequest.getRedirectUrl();
                    }
                    response.sendRedirect(redirectUrl);
                }
            })
            .failureHandler(new AuthenticationFailureHandler() {
                @Override
                public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                    log.info("로그인 실패");
                    response.sendRedirect("/login");
                }
            })
            .permitAll();

        // logout
        http
            .logout() // 로그아웃 처리
            .logoutUrl("/logout") // 로그아웃 처리 URL
            .logoutSuccessUrl("/login") // 로그아웃 성공 후 이동페이지
            .deleteCookies("JSESSIONID","remember-me") // 로그아웃 후 쿠키 삭제
            .addLogoutHandler(new LogoutHandler() {
                @Override
                public void logout(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication) {
                    HttpSession session = request.getSession();
                    session.invalidate();
                }
            }) // 로그아웃 핸들러
            .logoutSuccessHandler(new LogoutSuccessHandler() {
                @Override
                public void onLogoutSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {
                    response.sendRedirect("/login");
                }
            }) // 로그아웃 성공 후 핸들러
            .deleteCookies("remember-me") // 쿠키 삭제
            .and()
            .rememberMe()
            .rememberMeParameter("remember-me")
            .tokenValiditySeconds(3600) // Default 는 14일
            .userDetailsService(users());

        http
            .sessionManagement()
            .maximumSessions(1)
            // 동시 세션 제어
            // false(default) : 이전 사용자의 세션 만료
            // true : 신규 사용자의 인증 실패
            .maxSessionsPreventsLogin(false)
            .and()
            // 세션 고정 보호
            .sessionFixation().changeSessionId();

        http
            .exceptionHandling()
//            .authenticationEntryPoint(new AuthenticationEntryPoint() {
//                @Override
//                public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//                    response.sendRedirect("/login");
//                }
//            })
            .accessDeniedHandler(new AccessDeniedHandler() {
                @Override
                public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                    response.sendRedirect("/denied");
                }
            });

        return http.build();
    }

}
