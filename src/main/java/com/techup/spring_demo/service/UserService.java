package com.techup.spring_demo.service;

import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.entity.AuthProvider;
import com.techup.spring_demo.entity.UserProfileEntity;
import com.techup.spring_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. ฟังก์ชันสมัครสมาชิก
    public UserEntity registerUser(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("อีเมลนี้ถูกใช้งานแล้ว กรุณาใช้อีเมลอื่น");
        }
        user.setAuthProvider(AuthProvider.LOCAL);
        return userRepository.save(user);
    }

    // 2. ฟังก์ชันเข้าสู่ระบบ
    public UserEntity loginUser(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("ไม่พบบัญชีผู้ใช้นี้ในระบบ"));

        if (password == null || !password.equals(user.getPassword())) {
            throw new RuntimeException("รหัสผ่านไม่ถูกต้อง");
        }

        return user;
    }

// 3. ฟังก์ชันอัปเดตโปรไฟล์ (รองรับโครงสร้างใหม่ 2 ตาราง)
    public UserEntity updateUser(Long id, UserEntity updatedData) {
    UserEntity existingUser = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูลผู้ใช้งาน"));

    // 3.1 อัปเดตข้อมูลตาราง users
    if (updatedData.getUsername() != null) existingUser.setUsername(updatedData.getUsername());
    if (updatedData.getAvatarUrl() != null) existingUser.setAvatarUrl(updatedData.getAvatarUrl());

    // 3.2 จัดการข้อมูลตาราง user_profiles
    if (updatedData.getProfile() != null) {
        UserProfileEntity profile = existingUser.getProfile();
        
        // ถ้ายังไม่เคยมี Profile ในระบบ ให้สร้างใหม่
        if (profile == null) {
            profile = new UserProfileEntity();
            profile.setUser(existingUser);
        }

        // นำข้อมูลใหม่มาใส่
        if (updatedData.getProfile().getNickname() != null) profile.setNickname(updatedData.getProfile().getNickname());
        if (updatedData.getProfile().getBio() != null) profile.setBio(updatedData.getProfile().getBio());
        if (updatedData.getProfile().getGender() != null) profile.setGender(updatedData.getProfile().getGender());
        if (updatedData.getProfile().getBirthdate() != null) profile.setBirthdate(updatedData.getProfile().getBirthdate());
        if (updatedData.getProfile().getSocialLink() != null) profile.setSocialLink(updatedData.getProfile().getSocialLink());
        if (updatedData.getProfile().getCoverUrl() != null) profile.setCoverUrl(updatedData.getProfile().getCoverUrl());

        existingUser.setProfile(profile);
    }

    return userRepository.save(existingUser);
    }

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบผู้ใช้งานนี้ในระบบ"));
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("ไม่พบผู้ใช้งานนี้"));
    }
}