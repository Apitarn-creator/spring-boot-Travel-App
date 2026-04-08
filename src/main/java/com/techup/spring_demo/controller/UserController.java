package com.techup.spring_demo.controller;

import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.techup.spring_demo.security.JwtUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // อนุญาตให้หน้าเว็บ Vue ส่งข้อมูลมาได้
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

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
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String password = payload.get("password");

            // ดึงข้อมูลผู้ใช้มาตรวจสอบ (ผ่าน UserService ของคุณ)
            UserEntity user = userService.loginUser(email, password);
            
            // 💡 สร้าง Token (บัตรพนักงาน) จากอีเมล
            String jwtToken = jwtUtils.generateTokenFromEmail(user.getEmail());

            user.setPassword(null); // ซ่อนรหัสผ่านไว้ ไม่ส่งกลับไปหน้าบ้าน

            // 💡 แพ็ค Token และข้อมูล User ส่งกลับไปเป็นคู่กัน
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", jwtToken);
            responseBody.put("user", user);

            return ResponseEntity.ok(responseBody);

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

    // รับ Request แบบ GET ที่ URL: /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        try {
            UserEntity user = userService.getUserById(id);
            // 💡 แอบลบรหัสผ่านทิ้งก่อนส่งกลับไปหน้าบ้าน (เพื่อความปลอดภัย)
            user.setPassword(null); 
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 💡 Endpoint ใหม่ สำหรับดึงข้อมูลโปรไฟล์จาก Username
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            UserEntity user = userService.getUserByUsername(username);
            user.setPassword(null); // ซ่อนรหัสผ่านเพื่อความปลอดภัย
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}