package com.techup.spring_demo.security;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // 💡 ตัวเข้ารหัสผ่าน (จำเป็นต้องมีใน Spring Security)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 💡 กฎระเบียบของระบบเรา
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // อนุญาตหน้าบ้านต่อเข้ามา
            .csrf(csrf -> csrf.disable()) // ปิดระบบกันแฮ็กแบบเก่า (เพราะเราใช้ JWT แล้ว)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ไม่ใช้ Session แบบดั้งเดิม
            .authorizeHttpRequests(auth -> auth
                // 🟢 โซนฟรี (ไม่ต้องมี Token ก็เข้าได้)
                .requestMatchers("/api/users/login", "/api/users/register").permitAll() // สมัคร/ล็อกอิน
                .requestMatchers(HttpMethod.GET, "/api/trips/**").permitAll() // อ่านทริป
                .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll() // ดูโปรไฟล์คนอื่น
                .requestMatchers("/api/users/google-login").permitAll()
                // 🔴 โซนหวงห้าม (ที่เหลือทั้งหมด เช่น สร้างทริป, คอมเมนต์ ต้องมี Token!)
                .anyRequest().authenticated()
            );

        // นำ รปภ. (JwtAuthFilter) ไปยืนดักไว้ก่อนถึงประตูเข้า Controller
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 💡 ตั้งค่า CORS ให้หน้าบ้าน (Vue.js) เรียก API ได้
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // URL ของหน้าบ้าน
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}