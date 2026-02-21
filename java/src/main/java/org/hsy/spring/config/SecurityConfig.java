package org.hsy.spring.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.service.UserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailService userDetailsService;
    private final Environment env;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        if (!isProd) {
            http.csrf(csrf -> csrf.disable());
        }

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/signup", "/index", "/",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // 게시판 조회는 누구나 가능
                        .requestMatchers("/board", "/board/{postNo}", "/api/board/filter").permitAll()
                        .requestMatchers("/api/board/{postNo}/like").authenticated()

                        // 작성/수정/삭제는 인증 필요
                        .requestMatchers("/board/write", "/board/edit/**").authenticated()
                        .requestMatchers("/api/board/**").authenticated()
                        .requestMatchers("/api/comments/**").authenticated()

                        // 비회원 식단 관리 페이지
                        .requestMatchers("/diet-entry", "/api/diet/**", "/diet", "/health_setup").permitAll()
                        .requestMatchers("/api/user/**").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/index", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        // CSRF 활성화 시 로그아웃을 POST로 처리해야 하므로 아래 설정 확인 필요
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"message\":\"로그인이 필요합니다.\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}