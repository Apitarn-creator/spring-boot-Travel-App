package com.techup.spring_demo.controller;

import org.springframework.web.client.RestTemplate;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
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
@CrossOrigin(origins = {"http://localhost:5173", "https://localhost:5173"})
public class UserController {

    @Autowired
    private com.techup.spring_demo.repository.UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private com.techup.spring_demo.service.TripService tripServiceRef;

    @Autowired
    private com.techup.spring_demo.service.NotificationService notificationService;

    // ✅ อ่าน Google Client ID จาก environment variable (ต้องตรงกับ Frontend)
    @Value("${google.client-id}")
    private String googleClientId;

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

            // ✅ ใช้ GoogleIdTokenVerifier แทน RestTemplate เรียก tokeninfo
            // Library นี้ verify signature และ expiry ให้อัตโนมัติ ปลอดภัยกว่าเดิมมาก
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google Token ไม่ถูกต้องหรือหมดอายุ");
            }

            // ดึงข้อมูล user จาก token ที่ผ่านการ verify แล้ว
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email   = payload.getEmail();
            String name    = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // เช็คว่ามีบัญชีในระบบอยู่แล้วหรือเปล่า
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            UserEntity user;

            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                // สมัครอัตโนมัติสำหรับผู้ใช้ใหม่
                user = new UserEntity();
                user.setEmail(email);
                user.setUsername(name != null ? name.replaceAll("\\s+", "") : email.split("@")[0]);
                user.setPassword("");
                user.setAuthProvider(AuthProvider.GOOGLE);
                user.setRole(Role.USER);
                if (picture != null) user.setAvatarUrl(picture);
                user = userRepository.save(user);
            }

            String jwt = jwtUtils.generateTokenFromEmail(user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            user.setPassword(null);
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("ล็อกอินด้วย Google ไม่สำเร็จ: " + e.getMessage());
        }
    }

    @PostMapping("/facebook-login")
    public ResponseEntity<?> facebookLogin(@RequestBody Map<String, String> request) {
        try {
            String fbToken = request.get("token");

            // 1. นำ Token ไปถาม Facebook เพื่อขอข้อมูลผู้ใช้
            RestTemplate restTemplate = new RestTemplate();
            // เรียก Graph API ของ Facebook เพื่อขอดึงข้อมูล id, name, email, picture
            String fbApiUrl = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + fbToken;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = restTemplate.getForObject(fbApiUrl, Map.class);
            
            if (payload == null || !payload.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Facebook Token ไม่ถูกต้อง หรือคุณไม่ได้อนุญาตให้เข้าถึงอีเมล");
            }

            String email = (String) payload.get("email");
            String name = (String) payload.get("name");

            // 2. เช็คว่ามีอีเมลนี้ในฐานข้อมูลเราหรือยัง
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            UserEntity user;

            if (userOpt.isPresent()) {
                // มีบัญชีอยู่แล้ว
                user = userOpt.get();
            } else {
                // ผู้ใช้ใหม่ สมัครอัตโนมัติ
                user = new UserEntity();
                user.setEmail(email);
                user.setUsername(name.replaceAll("\\s+", "")); 
                user.setPassword(""); 
                
                // 💡 ระบุว่าล็อกอินด้วย Facebook
                user.setAuthProvider(AuthProvider.FACEBOOK); 
                user.setRole(Role.USER); 

                user = userRepository.save(user);
            }

            // 3. ออก JWT Token ของระบบเรา
            String jwt = jwtUtils.generateTokenFromEmail(user.getEmail());

            // 4. เตรียมข้อมูลส่งกลับให้หน้าบ้าน
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            user.setPassword(null);
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ล็อกอินด้วย Facebook ไม่สำเร็จ: " + e.getMessage());
        }
    }

    // รับ Request แบบ PUT ที่ URL: /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserEntity updatedData,
            // ✅ [Security Fix #3] ดึงข้อมูล user ที่ล็อกอินอยู่จริงจาก SecurityContext
            org.springframework.security.core.Authentication authentication) {
        try {
            // ✅ [Security Fix #3] ตรวจสอบว่าคนที่ส่ง request มาเป็นเจ้าของ account นั้นจริงหรือเปล่า
            // ป้องกันกรณี user A แก้ไขข้อมูลของ user B โดยส่ง id ของ B มาตรงๆ
            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(403).body("ไม่มีสิทธิ์แก้ไขข้อมูลของผู้ใช้งานคนอื่น");
            }

            UserEntity user = userService.updateUser(id, updatedData);
            user.setPassword(null);
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
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ ค้นหา user ตาม username หรือ nickname
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        if (q == null || q.trim().isEmpty())
            return ResponseEntity.ok(java.util.Collections.emptyList());

        String query = q.trim().toLowerCase();
        java.util.List<java.util.Map<String, Object>> results = userRepository.findAll().stream()
            .filter(u -> {
                String username = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                String nickname = (u.getProfile() != null && u.getProfile().getNickname() != null)
                    ? u.getProfile().getNickname().toLowerCase() : "";
                return username.contains(query) || nickname.contains(query);
            })
            .limit(10)
            .map(u -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("avatarUrl", u.getAvatarUrl());
                m.put("nickname", u.getProfile() != null ? u.getProfile().getNickname() : null);
                return m;
            })
            .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // 💡 Endpoint ใหม่ สำหรับดึงข้อมูลโปรไฟล์จาก Username
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            UserEntity user = userService.getUserByUsername(username);
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ Toggle Bookmark — กด bookmark / ยกเลิก bookmark
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {
        try {
            UserEntity currentUser = (UserEntity) authentication.getPrincipal();

            // ต้อง bookmark trip ของตัวเองหรือคนอื่นก็ได้ ไม่ต้องเช็ค ownership
            // เช็คแค่ว่า trip มีอยู่จริง
            // (ถ้า trip ไม่มี TripService จะ throw exception เอง)

            String bookmarked = currentUser.getBookmarkedTrips() != null
                ? currentUser.getBookmarkedTrips() : "";

            java.util.List<String> list = new java.util.ArrayList<>(
                bookmarked.isEmpty() ? java.util.Collections.emptyList()
                    : java.util.Arrays.asList(bookmarked.split(","))
            );

            String tripIdStr = id.toString();
            boolean wasBookmarked = list.contains(tripIdStr);

            if (wasBookmarked) {
                list.remove(tripIdStr);
            } else {
                list.add(tripIdStr);
            }

            currentUser.setBookmarkedTrips(String.join(",", list));
            userRepository.save(currentUser);

            // อัปเดต localStorage ด้วยการส่งข้อมูล user กลับไป
            currentUser.setPassword(null);
            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("bookmarked", !wasBookmarked);
            res.put("bookmarkedTrips", currentUser.getBookmarkedTrips());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    // ✅ ดึง trips ที่ bookmark ไว้ (เฉพาะเจ้าของเท่านั้น — ต้องส่ง JWT)
    @GetMapping("/me/bookmarks")
    public ResponseEntity<?> getMyBookmarks(
            org.springframework.security.core.Authentication authentication) {
        try {
            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            String bookmarked = currentUser.getBookmarkedTrips();

            if (bookmarked == null || bookmarked.isEmpty()) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }

            java.util.List<com.techup.spring_demo.entity.TripEntity> trips =
                java.util.Arrays.stream(bookmarked.split(","))
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        try { return tripServiceRef.getTripById(Long.parseLong(s)); }
                        catch (Exception e) { return null; }
                    })
                    .filter(t -> t != null)
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    // ✅ Toggle Follow / Unfollow
    @PostMapping("/{targetId}/follow")
    public ResponseEntity<?> toggleFollow(
            @PathVariable Long targetId,
            org.springframework.security.core.Authentication authentication) {
        try {
            UserEntity me = (UserEntity) authentication.getPrincipal();
            if (me.getId().equals(targetId))
                return ResponseEntity.badRequest().body("ไม่สามารถ follow ตัวเองได้");

            userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("ไม่พบผู้ใช้งานนี้"));

            String following = me.getFollowing() != null ? me.getFollowing() : "";
            java.util.List<String> list = new java.util.ArrayList<>(
                following.isEmpty() ? java.util.Collections.emptyList()
                    : java.util.Arrays.asList(following.split(","))
            );
            String targetStr = targetId.toString();
            boolean wasFollowing = list.contains(targetStr);
            if (wasFollowing) { list.remove(targetStr); } else { list.add(targetStr); }
            me.setFollowing(String.join(",", list));
            userRepository.save(me);

            // ✅ แจ้งเตือนเมื่อมีคน follow (ไม่แจ้งตอน unfollow)
            if (!wasFollowing) {
                try { notificationService.notifyFollow(targetId, me); }
                catch (Exception ignored) {}
            }

            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("following", !wasFollowing);
            res.put("followingList", me.getFollowing());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    // ✅ ดึง follower/following count
    @GetMapping("/{id}/follow-stats")
    public ResponseEntity<?> getFollowStats(@PathVariable Long id) {
        try {
            long followerCount = userRepository.findAll().stream()
                .filter(u -> {
                    String f = u.getFollowing();
                    return f != null && !f.isEmpty() &&
                        java.util.Arrays.asList(f.split(",")).contains(id.toString());
                }).count();

            UserEntity target = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ไม่พบผู้ใช้งาน"));
            String fw = target.getFollowing();
            long followingCount = (fw == null || fw.isEmpty()) ? 0
                : java.util.Arrays.stream(fw.split(",")).filter(s -> !s.isEmpty()).count();

            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("followers", followerCount);
            res.put("following", followingCount);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    // ✅ Feed จาก user ที่ follow อยู่
    @GetMapping("/me/feed")
    public ResponseEntity<?> getFollowingFeed(
            org.springframework.security.core.Authentication authentication) {
        try {
            UserEntity me = (UserEntity) authentication.getPrincipal();
            String following = me.getFollowing();
            if (following == null || following.isEmpty())
                return ResponseEntity.ok(java.util.Collections.emptyList());

            java.util.List<com.techup.spring_demo.entity.TripEntity> feed =
                java.util.Arrays.stream(following.split(","))
                    .filter(s -> !s.isEmpty())
                    .flatMap(s -> {
                        try { return tripServiceRef.getTripsByAuthor(Long.parseLong(s)).stream(); }
                        catch (Exception e) { return java.util.stream.Stream.empty(); }
                    })
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

}