package com.javarush.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/users/deleted/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/users/**/restore").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/tasks/deleted/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/tasks/**/restore").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/tasks/*/assignees/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tasks/*/assignees/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tasks/*/assignees/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tasks/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks").hasAnyRole("GUEST", "USER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/*").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tasks/*/status").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/owner/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/assignee/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/status/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
