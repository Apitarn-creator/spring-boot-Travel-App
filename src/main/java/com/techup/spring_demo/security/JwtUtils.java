package com.techup.spring_demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // 💡 รหัสลับสำหรับเซ็นรับรอง Token (ต้องมีความยาวระดับนึง ห้ามบอกใคร!)
    private final String jwtSecret = "TravelBetterSecretKeyForJwtAuthentication1234567890";
    
    // 💡 เวลาหมดอายุของ Token (ตั้งไว้ที่ 24 ชั่วโมง = 86,400,000 มิลลิวินาที)
    private final int jwtExpirationMs = 86400000;

    // ฟังก์ชันแปลงตัวอักษรให้กลายเป็นกุญแจเข้ารหัส
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 🟢 1. สร้าง Token จาก Email (ออกบัตรพนักงาน)
    public String generateTokenFromEmail(String email) {
        return Jwts.builder()
                .setSubject(email) // เอา Email มาเป็นข้อมูลหลักในบัตร
                .setIssuedAt(new Date()) // วันเวลาที่ออกบัตร
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // วันหมดอายุ
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // เซ็นรับรองด้วยกุญแจลับ
                .compact();
    }

    // 🔵 2. ดึง Email ออกมาจาก Token (อ่านชื่อจากหน้าบัตร)
    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 🟡 3. ตรวจสอบว่า Token ถูกต้องและยังไม่หมดอายุใช่ไหม (เครื่องแสกนบัตร)
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true; // บัตรจริง ผ่านได้!
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT error: " + e.getMessage()); // บัตรปลอม หรือหมดอายุ
        }
        return false;
    }
}