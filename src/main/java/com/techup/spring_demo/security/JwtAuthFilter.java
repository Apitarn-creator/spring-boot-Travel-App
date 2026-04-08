package com.techup.spring_demo.security;

import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. ดึง Token ออกมาจาก Header ที่ชื่อว่า "Authorization"
            String jwt = parseJwt(request);

            // 2. ถ้ามี Token และ Token ถูกต้อง (ไม่หมดอายุ/ไม่ปลอมแปลง)
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                
                // 3. อ่านอีเมลจาก Token แล้วไปค้นหาใน Database
                String email = jwtUtils.getEmailFromJwtToken(jwt);
                Optional<UserEntity> userOpt = userRepository.findByEmail(email);

                if (userOpt.isPresent()) {
                    UserEntity user = userOpt.get();
                    
                    // 4. สร้างป้ายยืนยันตัวตน (Authentication) แล้วแปะไว้ที่ SecurityContext (อนุญาตให้ผ่านด่านได้)
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            System.err.println("ไม่สามารถยืนยันตัวตนผู้ใช้งานได้: " + e.getMessage());
        }

        // 5. ปล่อยให้ Request เดินทางต่อไปยัง Controller
        filterChain.doFilter(request, response);
    }

    // ฟังก์ชันตัดคำว่า "Bearer " ออก เพื่อเอาแค่ตัว Token เพียวๆ
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}