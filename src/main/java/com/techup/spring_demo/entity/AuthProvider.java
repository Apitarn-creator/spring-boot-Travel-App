package com.techup.spring_demo.entity;

public enum AuthProvider {
    LOCAL,     // สมัครผ่านเว็บเราปกติ
    GOOGLE,    // สมัครผ่าน Google
    FACEBOOK;  // สมัครผ่าน Facebook (ตัวสุดท้ายให้ปิดด้วย ; หรือไม่ต้องใส่เครื่องหมายใดๆ)
}