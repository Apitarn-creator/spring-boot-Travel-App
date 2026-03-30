package com.techup.spring_demo.repository;

import com.techup.spring_demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    // ค้นหาผู้ใช้จาก Email (ใช้ตอน Login)
    Optional<UserEntity> findByEmail(String email);

    // เช็คว่า Email นี้มีคนใช้สมัครไปหรือยัง (ใช้ตอน Register)
    boolean existsByEmail(String email);
}