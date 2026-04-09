package com.techup.spring_demo.controller;

import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import com.techup.spring_demo.entity.AuthProvider;
import com.techup.spring_demo.entity.Role;

import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.techup.spring_demo.security.JwtUtils;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // อนุญาตให้หน้าเว็บ Vue ส่งข้อมูลมาได้
public class UserController {

    @Autowired
    private com.techup.spring_demo.repository.UserRepository userRepository;

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

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        try {
            String googleToken = request.get("token");

            // 1. นำ Token ไปถาม Google เพื่อขอข้อมูลผู้ใช้
            RestTemplate restTemplate = new RestTemplate();
            String googleApiUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + googleToken;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = restTemplate.getForObject(googleApiUrl, Map.class);
            
            if (payload == null || !payload.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google Token ไม่ถูกต้อง");
            }

            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // 2. เช็คว่ามีอีเมลนี้ในฐานข้อมูลเราหรือยัง
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            UserEntity user;

            if (userOpt.isPresent()) {
                // ถ้ามีบัญชีอยู่แล้ว ก็ดึงข้อมูลมาใช้ได้เลย
                user = userOpt.get();
            } else {
                // 💡 ถ้าเป็นผู้ใช้ใหม่ (เพิ่งล็อกอินครั้งแรก) ให้สมัครสมาชิกอัตโนมัติ!
                user = new UserEntity();
                user.setEmail(email);
                
                // ตั้งชื่อ Username ให้โดยตัดช่องว่างออกจากชื่อ Google (เช่น "John Doe" -> "JohnDoe")
                user.setUsername(name.replaceAll("\\s+", "")); 
                
                // รหัสผ่านไม่ต้องใช้ เพราะล็อกอินด้วย Google (หรือตั้งเป็นค่าสุ่มไปเลยก็ได้)
                user.setPassword(""); 

                user.setAuthProvider(AuthProvider.GOOGLE); 
                user.setRole(Role.USER);
                
                // ดึงรูปโปรไฟล์จาก Google มาตั้งให้เลย
                // user.setAvatarUrl(picture); // เอาคอมเมนต์ออกถ้าใน Entity คุณมีฟิลด์ avatarUrl
                
                user = userRepository.save(user);
            }

            // 3. ออก JWT Token ของระบบเราเองให้ผู้ใช้งาน
            String jwt = jwtUtils.generateTokenFromEmail(user.getEmail());

            // 4. เตรียมข้อมูลส่งกลับไปให้หน้าบ้าน (รูปแบบเดียวกับตอน Login ปกติ)
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            user.setPassword(null); // ซ่อนรหัสผ่านเพื่อความปลอดภัยก่อนส่ง
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ล็อกอินด้วย Google ไม่สำเร็จ: " + e.getMessage());
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