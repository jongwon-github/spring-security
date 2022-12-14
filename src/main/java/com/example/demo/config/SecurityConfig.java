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
            // ?????? ??? ??????
            .authorizeRequests()
            .antMatchers("/user").hasRole("USER")
            .antMatchers("/admin/pay").hasRole("ADMIN")
            .antMatchers("/admin/**").access("hasRole('ADMIN') or hasRole('SYS')")
            .anyRequest()
            .authenticated();

        // login
        http
            .formLogin()
            //.loginPage("/loginPage") ?????? ????????? login ????????? ????????? ?????? ??????, ??? ?????? ???????????? spring security ?????? ???????????? ?????????  ???????????? ??????
            .defaultSuccessUrl("/") // ????????? ?????? ??? ?????? ?????????
            .usernameParameter("username") // ????????? ??????????????? ??????
            .passwordParameter("password") // ???????????? ??????????????? ??????
            .loginProcessingUrl("/login")
            .successHandler(new AuthenticationSuccessHandler() {
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                    log.info("????????? ??????");
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
                    log.info("????????? ??????");
                    response.sendRedirect("/login");
                }
            })
            .permitAll();

        // logout
        http
            .logout() // ???????????? ??????
            .logoutUrl("/logout") // ???????????? ?????? URL
            .logoutSuccessUrl("/login") // ???????????? ?????? ??? ???????????????
            .deleteCookies("JSESSIONID","remember-me") // ???????????? ??? ?????? ??????
            .addLogoutHandler(new LogoutHandler() {
                @Override
                public void logout(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication) {
                    HttpSession session = request.getSession();
                    session.invalidate();
                }
            }) // ???????????? ?????????
            .logoutSuccessHandler(new LogoutSuccessHandler() {
                @Override
                public void onLogoutSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {
                    response.sendRedirect("/login");
                }
            }) // ???????????? ?????? ??? ?????????
            .deleteCookies("remember-me") // ?????? ??????
            .and()
            .rememberMe()
            .rememberMeParameter("remember-me")
            .tokenValiditySeconds(3600) // Default ??? 14???
            .userDetailsService(users());

        http
            .sessionManagement()
            .maximumSessions(1)
            // ?????? ?????? ??????
            // false(default) : ?????? ???????????? ?????? ??????
            // true : ?????? ???????????? ?????? ??????
            .maxSessionsPreventsLogin(false)
            .and()
            // ?????? ?????? ??????
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
