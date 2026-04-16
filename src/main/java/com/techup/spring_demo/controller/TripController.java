package com.techup.spring_demo.controller;

import com.techup.spring_demo.entity.TripEntity;
import com.techup.spring_demo.entity.TripCommentEntity;
import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.service.TripService;
import com.techup.spring_demo.repository.TripCommentRepository;
import com.techup.spring_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = {"http://localhost:5173", "https://localhost:5173"})
public class TripController {

    @Autowired
    private TripService tripService;
    
    @Autowired
    private TripCommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TripEntity>> getAllTrips(
         @RequestParam(required = false) String search) { // รับค่า search แบบไม่บังคับ
            
        List<TripEntity> trips = tripService.getAllTrips(search);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tripService.getTripById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 👇 1. API ดึงคอมเมนต์ทั้งหมดของทริปนั้น 👇
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TripCommentEntity>> getComments(@PathVariable Long id) {
        List<TripCommentEntity> comments = commentRepository.findByTripIdOrderByIdDesc(id);
        // ซ่อนรหัสผ่านของผู้คอมเมนต์เพื่อความปลอดภัย
        comments.forEach(c -> { if (c.getUser() != null) c.getUser().setPassword(null); });
        return ResponseEntity.ok(comments);
    }

    // 👇 2. API สำหรับส่งคอมเมนต์ใหม่ 👇
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            // ✅ [Security Fix #4] ดึง user จาก JWT token โดยตรง ไม่เชื่อ userId จาก request body
            // ก่อนหน้านี้ ผู้ใช้สามารถส่ง userId ของคนอื่นมาแล้ว comment ในชื่อคนนั้นได้!
            org.springframework.security.core.Authentication authentication) {
        try {
            // ✅ [Security Fix #4] userId ที่แท้จริงมาจาก Token ที่ผ่านการ verify แล้ว ไม่ใช่จาก body
            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            String content = payload.get("content");

            TripCommentEntity comment = new TripCommentEntity();
            comment.setTripId(id);
            comment.setContent(content);
            comment.setUser(currentUser);

            commentRepository.save(comment);
            
            currentUser.setPassword(null);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ไม่สามารถเพิ่มความเห็นได้");
        }
    }

    // 3. Endpoint สำหรับสร้างทริปใหม่ (POST /api/trips)
    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody TripEntity trip) {
        try {
            TripEntity savedTrip = tripService.createTrip(trip);
            return ResponseEntity.ok(savedTrip);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ไม่สามารถสร้างทริปได้: " + e.getMessage());
        }
    }

    // ✅ Like / Unlike endpoint
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {
        try {
            com.techup.spring_demo.entity.UserEntity currentUser =
                (com.techup.spring_demo.entity.UserEntity) authentication.getPrincipal();
            Long userId = currentUser.getId();

            TripEntity trip = tripService.getTripById(id);

            // ใช้ comma-separated string แทน array
            String likedByStr = trip.getLikedByStr() != null ? trip.getLikedByStr() : "";
            java.util.List<String> likedList = new java.util.ArrayList<>(
                likedByStr.isEmpty() ? java.util.Collections.emptyList()
                    : java.util.Arrays.asList(likedByStr.split(","))
            );
            boolean alreadyLiked = likedList.contains(userId.toString());

            if (alreadyLiked) {
                likedList.remove(userId.toString());
                trip.setLikes(Math.max(0, (trip.getLikes() != null ? trip.getLikes() : 0) - 1));
            } else {
                likedList.add(userId.toString());
                trip.setLikes((trip.getLikes() != null ? trip.getLikes() : 0) + 1);
            }

            trip.setLikedByStr(String.join(",", likedList));

            TripEntity saved = tripService.saveTrip(trip);
            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("likes", saved.getLikes());
            res.put("liked", !alreadyLiked);
            // ส่ง likedBy เป็น array ให้ frontend ใช้เช็คได้
            res.put("likedBy", likedList.stream().map(Long::parseLong).collect(java.util.stream.Collectors.toList()));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }
    // Endpoint ดึงทริปที่ตัวเองสร้าง
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TripEntity>> getTripsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(tripService.getTripsByAuthor(userId));
    }

    // Endpoint ดึงทริปที่ตัวเองเคยไปคอมเมนต์
    @GetMapping("/user/{userId}/commented")
    public ResponseEntity<List<TripEntity>> getCommentedTrips(@PathVariable Long userId) {
        return ResponseEntity.ok(tripService.getTripsCommentedByUser(userId));
    }
}