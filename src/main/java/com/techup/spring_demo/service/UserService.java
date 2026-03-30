package com.techup.spring_demo.service;

import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.entity.AuthProvider;
import com.techup.spring_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserEntity registerUser(UserEntity user) {
        // 1. ตรวจสอบว่าอีเมลนี้ซ้ำหรือไม่
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("อีเมลนี้ถูกใช้งานแล้ว กรุณาใช้อีเมลอื่น");
        }

        // 2. กำหนดค่าเริ่มต้นว่าเป็นการสมัครผ่านเว็บ (LOCAL)
        user.setAuthProvider(AuthProvider.LOCAL);

        // หมายเหตุ: ตอนนี้เราจะเซฟรหัสผ่านลงไปตรงๆ ก่อนเพื่อทดสอบระบบ 
        // (เดี๋ยวเราค่อยมาเพิ่มระบบเข้ารหัส BCrypt ทีหลัง เพื่อไม่ให้กระทบ API เดิมของคุณครับ)

        // 3. บันทึกลงฐานข้อมูล
        return userRepository.save(user);
    }

    // เพิ่มฟังก์ชันนี้ต่อท้ายฟังก์ชัน registerUser
    public UserEntity loginUser(String email, String password) {
        // 1. ค้นหาผู้ใช้จากอีเมล
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("ไม่พบบัญชีผู้ใช้นี้ในระบบ"));

        // 2. เทียบรหัสผ่าน (ตอนนี้เรายังเก็บเป็นข้อความธรรมดา เลยเทียบตรงๆ ได้เลย)
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("รหัสผ่านไม่ถูกต้อง");
        }

        return user;
    }

    // ฟังก์ชันสำหรับอัปเดตโปรไฟล์
    public UserEntity updateUser(Long id, UserEntity updatedData) {
        // 1. หา User เดิมในระบบ
        UserEntity existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูลผู้ใช้งาน"));

        // 2. อัปเดตข้อมูล (เปลี่ยนชื่อ และ รูปโปรไฟล์)
        existingUser.setUsername(updatedData.getUsername());
        
        if (updatedData.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(updatedData.getAvatarUrl());
        }

        // 3. เซฟทับลงฐานข้อมูล
        return userRepository.save(existingUser);
    }
}