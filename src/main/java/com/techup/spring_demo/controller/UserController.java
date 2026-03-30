package com.techup.spring_demo.controller;

import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // อนุญาตให้หน้าเว็บ Vue ส่งข้อมูลมาได้
public class UserController {

    @Autowired
    private UserService userService;

    // เปิดรับ Request แบบ POST ที่ URL: /api/users/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserEntity user) {
        try {
            // ส่งข้อมูลไปให้ Service จัดการเซฟ
            UserEntity savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
            
        } catch (RuntimeException e) {
            // ถ้าอีเมลซ้ำ หรือมี Error ให้ส่งข้อความแจ้งเตือนกลับไปที่หน้าบ้าน
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // เพิ่มฟังก์ชันนี้ต่อจากฟังก์ชัน registerUser
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            UserEntity user = userService.loginUser(email, password);
            return ResponseEntity.ok(user); // ถ้าสำเร็จ ส่งข้อมูลผู้ใช้กลับไปให้หน้าบ้าน
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // รับ Request แบบ PUT ที่ URL: /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserEntity updatedData) {
        try {
            UserEntity user = userService.updateUser(id, updatedData);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}