package org.hsy.spring.config;

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

        if(!isProd) {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/login", "/signup", "/index", "/", "/api/auth/**", "/css/**", "/js/**", "/images/**","/api/post").permitAll()
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
                    );

        }
        else{
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/login", "/signup", "/index", "/", "/api/auth/**", "/css/**", "/js/**", "/images/**","/api/post").permitAll()
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
                    );

        }
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