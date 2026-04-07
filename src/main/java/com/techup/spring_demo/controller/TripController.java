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
@CrossOrigin(origins = "http://localhost:5173")
public class TripController {

    @Autowired
    private TripService tripService;
    
    @Autowired
    private TripCommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<TripEntity>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
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
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Long userId = Long.parseLong(payload.get("userId"));
            String content = payload.get("content");

            UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ไม่พบผู้ใช้งาน"));

            TripCommentEntity comment = new TripCommentEntity();
            comment.setTripId(id);
            comment.setContent(content);
            comment.setUser(user);

            commentRepository.save(comment);
            
            user.setPassword(null); // ซ่อนรหัสผ่านก่อนส่งกลับ
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