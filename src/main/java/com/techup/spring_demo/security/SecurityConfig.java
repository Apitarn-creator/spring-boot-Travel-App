package com.techup.spring_demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // ✅ [Security Fix #5] อ่าน CORS origins จาก environment variable
    // ตอน dev จะเป็น http://localhost:5173
    // ตอน production ให้เซ็ต CORS_ALLOWED_ORIGINS=https://yourdomain.com
    @Value("${app.cors.allowed-origins:http://localhost:5173,https://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ✅ อนุญาต OPTIONS preflight ทุก URL — browser ส่งมาก่อน POST เสมอ
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 🟢 โซนฟรี
                .requestMatchers(HttpMethod.POST, "/api/users/google-login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/facebook-login").permitAll()
                .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/trips/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                // bookmark และ me endpoints ต้อง login ก่อน
                .requestMatchers("/api/users/me/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/users/*/bookmark").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/users/*/follow").authenticated()
                // 🔴 โซนหวงห้าม
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ [Security Fix #5] รองรับหลาย origins โดยอ่านจาก env (คั่นด้วย comma)
        // เช่น CORS_ALLOWED_ORIGINS=https://app.com,https://www.app.com
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}