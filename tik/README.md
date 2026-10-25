# Mobile Product REST API (INT261 Practical Exam)

ระบบจัดการสินค้าโทรศัพท์มือถือและแบรนด์ (Mobile Product & Brand Management API) พัฒนาด้วย **Spring Boot 4 / Java 17+** เชื่อมต่อฐานข้อมูล **MySQL 8.0+** พร้อมระบบ DTO, Data Validation, Error Handling, และ Transaction Management ตามข้อกำหนดของรายวิชา INT261

---

## 1. ข้อกำหนดของสภาพแวดล้อม (Prerequisites)

- **Java**: OpenJDK 17 หรือ 21 ขึ้นไป
- **Database**: MySQL 8.0+ (หรือ H2 In-Memory สำหรับโหมดทดสอบออฟไลน์)
- **Build Tool**: Apache Maven (แนะนำให้ใช้ Maven Wrapper `./mvnw` หรือ `mvnw.cmd` ที่แนบมากับโปรเจกต์)
- **Port เริ่มต้น**: `8080` (Base URL: `http://localhost:8080/api/v1`)

---

## 2. การตั้งค่าฐานข้อมูล (Database Configuration)

### 2.1 การเตรียมฐานข้อมูล MySQL
1. สร้างฐานข้อมูลชื่อ `mobile_exam` ใน MySQL:
   ```sql
   CREATE DATABASE mobile_exam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. นำเข้าตาราง (Schema) และข้อมูลเริ่มต้น (Seed Data) จากไฟล์ SQL ที่ได้รับก่อนเริ่มแอปพลิเคชัน
3. ผู้ใช้และสิทธิ์: กำหนดผู้ใช้ `exam` (รหัสผ่าน `exam`) ให้มีสิทธิ์เข้าถึงฐานข้อมูล `mobile_exam`

### 2.2 การเชื่อมต่อผ่าน Environment Variables
แอปพลิเคชันถูกตั้งค่าให้อ่านค่าการเชื่อมต่อผ่าน Environment Variables โดยมีค่าเริ่มต้น (fallback) ดังนี้:

| ตัวแปร (Environment Variable) | ค่าเริ่มต้น (Default Value) | คำอธิบาย |
|---|---|---|
| `SERVER_PORT` | `8080` | พอร์ตของเว็บเซิร์ฟเวอร์ |
| `DB_URL` | `jdbc:mysql://localhost:3306/mobile_exam` | JDBC URL สำหรับเชื่อมต่อ MySQL |
| `DB_USER` | `exam` | Username ของฐานข้อมูล |
| `DB_PASSWORD` | `exam` | Password ของฐานข้อมูล |

ตัวอย่างการตั้งค่าใน `src/main/resources/application.properties`:
```properties
server.port=${SERVER_PORT:8080}
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/mobile_exam}
spring.datasource.username=${DB_USER:exam}
spring.datasource.password=${DB_PASSWORD:exam}
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
```

*(หมายเหตุ: หากต้องการสลับไปทดสอบแบบออฟไลน์ด้วย H2 In-Memory สามารถสลับคอมเมนต์ใน `application.properties` ได้ทันที)*

---

## 3. วิธีการ Build และรันแอปพลิเคชัน (Build & Run)

### 3.1 การรันโดยตรงผ่าน Maven Wrapper
- **Windows (Command Prompt / PowerShell):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **macOS / Linux:**
  ```bash
  ./mvnw spring-boot:run
  ```

### 3.2 การ Build เป็น JAR File และรัน
1. ทำการ Compile และ Build แพ็กเกจ:
   ```bash
   ./mvnw clean package -DskipTests
   ```
2. รันไฟล์ JAR ที่ได้จากโฟลเดอร์ `target/`:
   ```bash
   java -jar target/int261-sample-exam-0.0.1-SNAPSHOT.jar
   ```

### 3.3 การรันชุดทดสอบ (Automated Testing)
- **Unit & Integration Tests (Maven):**
  ```bash
  ./mvnw test
  ```
- **Black-box End-to-End Test (Python):**
  ```bash
  python test_api.py --base-url http://localhost:8080
  ```

---

## 4. ภาพรวมสถาปัตยกรรมและโครงสร้างโค้ด (Architecture)

โปรเจกต์แยกเลเยอร์การทำงานอย่างเคร่งครัดตามมาตรฐาน RESTful Architecture:
- **Controller Layer (`sit.int261.int261sampleexam.controllers`)**:
  - `ProductController`: จัดการ HTTP Requests/Responses ของสินค้า, ตรวจสอบ path id แบบ positive decimal digits, Location URI
  - `BrandsControllers`: จัดการ endpoint แบรนด์ โดยส่งออก `BrandResponse` เท่านั้น (ไม่ส่ง Entity สู่ภายนอก)
  - ใช้ **Constructor Injection** ในทุก Controller
- **Service Layer (`sit.int261.int261sampleexam.services`)**:
  - `ProductService`: บรรจุ Business logic, การตรวจสอบเงื่อนไขความถูกต้อง และ DTO mapping
  - จัดการ Transaction ด้วย `@Transactional(readOnly = true)` และ `@Transactional` บน CUD operations
  - ตรวจสอบลำดับความถูกต้อง: `product exists` → `brand exists` → `SKU uniqueness`
- **Repository Layer (`sit.int261.int261sampleexam.repositories`)**:
  - `ProductRepository`, `BrandRepository`: เชื่อมต่อ Spring Data JPA พร้อมฟังก์ชันเรียงลำดับ `OrderByIdAsc`
- **DTOs & Exception Handling**:
  - แยก Request/Response DTO ออกจาก JPA Entity 100%
  - มี `GlobalExceptionHandler` (`@RestControllerAdvice`) คืน ErrorResponse พร้อม status code ตรงตามสเปก (400, 404, 405, 406, 409, 415, 500)

---

## 5. สรุป REST API Endpoints

ฐาน URL: `http://localhost:8080/api/v1`

| Method | Endpoint | รายละเอียด | ผลลัพธ์สำเร็จ |
|---|---|---|---|
| `GET` | `/brands` | ดึงข้อมูลแบรนด์ทั้งหมด เรียงตาม `id ASC` | `200 OK` (Array of BrandResponse) |
| `GET` | `/products` | ดึงสินค้าทั้งหมด เรียงตาม `id ASC` (รวม active=false) | `200 OK` (Array of ProductResponse) |
| `GET` | `/products?brandId={id}` | กรองสินค้าตามรหัสแบรนด์ เรียงตาม `id ASC` | `200 OK` (Array of ProductResponse) |
| `GET` | `/products/{id}` | อ่านข้อมูลสินค้ารายตัวตามรหัส id | `200 OK` (ProductResponse) |
| `POST` | `/products` | สร้างสินค้าใหม่ (คืน Location header) | `201 Created` + Location |
| `PUT` | `/products/{id}` | แทนที่ข้อมูลสินค้าทุกฟิลด์ที่เขียนได้ | `200 OK` (ProductResponse) |
| `DELETE` | `/products/{id}` | ลบสินค้าออกจากฐานข้อมูล | `204 No Content` |
