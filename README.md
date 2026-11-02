# 🏰 Secure da Tower

> **Tactical Defense 2D Game in Java**  
> รายงานและโครงงานส่วนหนึ่งของรายวิชา 040613204 Object-Oriented Programming  
> ภาควิชาวิทยาการคอมพิวเตอร์และสารสนเทศ คณะวิทยาศาสตร์ประยุกต์  
> มหาวิทยาลัยเทคโนโลยีพระจอมเกล้าพระนครเหนือ (KMUTNB)

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![GUI](https://img.shields.io/badge/UI-Java%20Swing%20%2F%20AWT-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-OOP%20%2F%20MVC-green.svg)

---

## 📖 เกี่ยวกับโปรเจกต์ (Project Overview)

**Secure da Tower** เป็นเกมแนว Tactical Defense 2D ที่เขียนขึ้นด้วยภาษา Java โดยใช้การประมวลผลกราฟิกและอินเทอร์เฟสผ่าน **Java Swing & AWT** ผู้เล่นรับบทเป็นผู้ควบคุมป้อมปราการหลักกลางหน้าจอที่ต้องหมุนปืนเล็งตามเมาส์ ยิงทำลายศัตรูประเภทต่างๆ ที่บุกรุกเข้ามา และจัดการเรียกใช้สกิลสนับสนุนทางการทหาร เช่น การโจมตีทางอากาศ (Airstrike) เพื่อปกป้องฐานทัพให้รอดพ้นจากคลื่นศัตรูในแต่ละด่าน (Level 1–10)

โปรเจกต์นี้ถูกออกแบบมาเพื่อศึกษาและนำหลักการ **Object-Oriented Programming (OOP)** มาประยุกต์ใช้ในการพัฒนาซอฟต์แวร์จริง เช่น การจัดการวงรอบเกม (Game Loop), สถาปัตยกรรมสถานะเกม (State Machine), การดักจับเหตุการณ์ (Event Handling) และการคำนวณการชน (Collision Detection)

---

## ✨ คุณสมบัติเด่น (Features)

- 🎯 **Mouse Aiming & Dynamic Shooting:** ป้อมปราการหมุนปากกระบอกปืนคำนวณองศาตามพิกัดเมาส์เรียลไทม์ และยิงกระสุนพุ่งตามทิศทาง
- 👾 **Multiple Enemy Types (Polymorphism):** ศัตรู 3 รูปแบบที่มีความเร็ว พลังชีวิต และขนาดขอบเขตการชนแตกต่างกัน:
  - `FastEnemy`: เคลื่อนที่เร็ว ความชำนาญสูง เลือดน้อย
  - `MediumEnemy`: ศัตรูขนาดกลาง สมดุลทุกด้าน
  - `TankEnemy`: ศัตรูตัวใหญ่ พลังชีวิตสูง เกราะหนา
- ✈️ **Airstrike Support System:** สกิลเรียกเครื่องบินรบมาทิ้งระเบิดถล่มเป้าหมายในรัศมี เมื่อเกจคูลดาวน์พร้อมใช้งาน
- 🪨 **Grid-based Obstacle Placement:** ระบบสุ่มวางสิ่งกีดขวาง (ก้อนหิน `OBS1`, `OBS2`, `OBS3`) ตามตารางแผนที่เพื่อบดบังวิถีกระสุน
- 📊 **Tactical HUD & Mission End Overlay:** แสดงระดับพลังชีวิต (HP), จำนวน Kills, ระดับด่าน (LVL), คูลดาวน์ Airstrike และหน้าต่างสรุปผลเมื่อจบภารกิจ (Sector Secured / Mission Failed)
- ⚙️ **Fixed Framerate Loop:** ควบคุมการอัปเดตระบบและการเรนเดอร์หน้าจอคงที่ (~60 FPS) ด้วย `javax.swing.Timer`

---

## 🎮 การควบคุมและปุ่มลัด (Controls)

| การกระทำ (Action) | ปุ่ม / อุปกรณ์ (Control) | คำอธิบาย (Description) |
| :--- | :--- | :--- |
| **เล็งป้อมปืน** | Move Mouse | หมุนองศาปากกระบอกปืนตามพิกัดเมาส์ |
| **ยิงกระสุน** | Left Mouse Button (LMB) | ยิงกระสุนออกจากป้อมปราการ |
| **เรียก Airstrike** | `E` Key | เรียกการโจมตีทางอากาศ ณ พิกัดเป้าหมายเมาส์ |
| **เริ่มเกมใหม่** | `R` Key | รีเซ็ตสถานะและเริ่มเล่นเกมใหม่ (Restart) |
| **เลือกเมนูหลัก** | Click Buttons | คลิกปุ่ม `START GAME` หรือ `EXIT` |

---

## 🛠️ หลักการ OOP ที่นำมาประยุกต์ใช้ (OOP Principles Used)

1. **Encapsulation (การห่อหุ้ม):** ซ่อนสถานะภายในคลาส เช่น `health`, `aimAngle` ด้วย `private` และเข้าถึงผ่าน getter/setter หรือเมธอดควบคุมความปลอดภัย (`takeDamage()`)
2. **Inheritance (การสืบทอด):** สืบทอดคุณสมบัติร่วมของศัตรูและสิ่งกีดขวางผ่าน Abstract Class เช่น `Enemy` $\rightarrow$ `FastEnemy`, `TankEnemy` และ `Obstacle` $\rightarrow$ `StaticObstacle`
3. **Polymorphism (พหุรูป):** การ Override เมธอดในคลาสลูก เช่น `getCollisionRadius()` หรือ `getRenderWidth()` ทำให้คลาสแม่สั่งประมวลผลขนาดเฉพาะตัวได้อัตโนมัติ
4. **Abstraction (การแยกคุณลักษณะ):** ใช้องค์ประกอบโครงสร้าง Abstract Class สำหรับ `Enemy` และ `Obstacle` เพื่อกำหนดอินเทอร์เฟซมาตรฐานของวัตถุภายในเกม
5. **Composition (การประกอบรวม):** `GameController` ทำหน้าที่รวมและควบคุมอ็อบเจ็กต์หลักทั้งหมด เช่น `Tower`, `LevelManager`, `AirstrikeSystem`, รายการ `Bullet`, `Enemy` และ `Explosion`

---

## 📁 โครงสร้างโปรเจกต์ (Project Structure)

```text
├── assets/                  # ไฟล์รูปภาพและทรัพยากรกราฟิก (PNG)
│   ├── Background.png
│   ├── Gunbase.png
│   ├── Gun.png
│   ├── Fast.png
│   ├── Medium.png
│   ├── Tank.png
│   ├── OBS1.png / OBS2.png / OBS3.png
│   ├── airplane.png
│   └── cursor.png
├── src/ (Root Directory)
│   ├── main.java            # คลาสหลักสร้าง JFrame, JPanel, Event Handling และ Game Loop
│   ├── GameController.java   # ตัวควบคุมตรรกะเกม การสร้างศัตรู และคำนวณสเตจ
│   ├── HudRenderer.java      # คลาสเรนเดอร์กราฟิก UI, HUD, เมนู และหน้าสรุปผล
│   ├── LevelManager.java    # จัดการการสุ่มแผนที่ ด่าน 1-10 และสร้างสิ่งกีดขวาง
│   ├── Tower.java           # อ็อบเจ็กต์ป้อมปราการหลักและการเล็งยิง
│   ├── Bullet.java          # อ็อบเจ็กต์กระสุนปืนและระบบตรวจจับการชน (Collision)
│   ├── Enemy.java           # Abstract Class ศัตรู และ Inner Classes (Fast, Medium, Tank)
│   ├── Obstacle.java        # Abstract Class สิ่งกีดขวางตามระบบ Grid
│   ├── StaticObstacle.java  # คลาสลูกของสิ่งกีดขวางคงที่
│   ├── AirstrikeSystem.java # ระบบสกิลเรียกเครื่องบินทิ้งระเบิดทางอากาศ
│   └── Explosion.java       # เอฟเฟกต์การระเบิดอนิเมชันแบบ Ring-Expansion
└── README.md                # เอกสารประกอบโปรเจกต์
```

---

## 🚀 วิธีการติดตั้งและเปิดใช้งาน (Getting Started)

### ความต้องการของระบบ (Prerequisites)
- Java Development Kit (JDK) **11** หรือเวอร์ชันที่สูงกว่า
- IDE รองรับภาษา Java (เช่น VS Code, IntelliJ IDEA, Eclipse, NetBeans)

### ขั้นตอนการรันโปรแกรม (Execution)

1. **Clone Repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Secure-da-Tower.git
   cd Secure-da-Tower
   ```

2. **Compile Source Code:**
   ```bash
   javac *.java
   ```

3. **Run Application:**
   ```bash
   java main
   ```

---

## 📄 เอกสารประกอบ

 [เอกสารโครงงาน](https://docs.google.com/document/d/1htmpm-MPtBR8PUGCcescMUqazsTM6DA-gko9WK0_UzM/edit?usp=sharing)

---
## 👨‍💻 ผู้จัดทำ (Developer)

* **นายปัญญวัฒน์ ฟักทิม** (6704062612162)  
* **อาจารย์ผู้สอน:** ผู้ช่วยศาสตราจารย์ สถิตย์ ประสมพันธ์  
* **ภาควิชาวิทยาการคอมพิวเตอร์และสารสนเทศ** คณะวิทยาศาสตร์ประยุกต์  
* **มหาวิทยาลัยเทคโนโลยีพระจอมเกล้าพระนครเหนือ (KMUTNB)**