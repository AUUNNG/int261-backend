# 📋 ใบงาน: การออกแบบ Data Transfer Object (DTO) และ Object Mapping
### เพิ่มเติม: รองรับ Pagination, Search และ Sorting

* **วิชา:** สถาปัตยกรรมซอฟต์แวร์และการพัฒนาเว็บเซอร์วิส (Web Services & Enterprise Application)
* **ฐานข้อมูลอ้างอิง:** MySQL Sample Database (`classicmodels`)
* **คำชี้แจง:** 
  * ให้นักศึกษาออกแบบ Data Transfer Object (DTO) และพัฒนา API ให้รองรับ **Pagination (การแบ่งหน้า)**, **Search (การค้นหา)**, และ **Sorting (การเรียงลำดับ)** ตามข้อกำหนด
  * นักศึกษาสามารถเลือกใช้เครื่องมือหรือเทคนิคที่ตนเองถนัดได้อย่างอิสระ (เช่น Spring Data `Pageable`, Specification/Criteria API, Manual Mapping, MapStruct, Java Record, หรือ POJO + Lombok)
  * โครงสร้างของ JSON Response และ Business Logic จะต้องถูกต้องตรงตาม Specification 100%

---

## 🌐 ข้อกำหนดมาตรฐานกลาง (Common Standards)

ทุก Endpoint ที่มีการดึงข้อมูลแบบรายการ (List / Collection) จะต้องใช้พารามิเตอร์และโครงสร้าง Response Envelope ร่วมกันดังนี้:

### 1. Standard Query Parameters
| Query Parameter | ชนิดข้อมูล | ค่าเริ่มต้น (Default) | คำอธิบาย |
| :--- | :---: | :---: | :--- |
| `page` | `Integer` | `0` | หมายเลขหน้า (เริ่มต้นที่ index 0) |
| `size` | `Integer` | `10` | จำนวนข้อมูลต่อ 1 หน้า (ขนาดข้อมูลที่ดึง) |
| `sortBy` | `String` | *(ตามแต่ละข้อ)* | ชื่อฟิลด์ที่ต้องการใช้จัดเรียง |
| `sortDir` | `String` | `"asc"` | ทิศทางการจัดเรียง (`"asc"` หรือ `"desc"`) |
| `keyword` | `String` | `null` | คำค้นหา (Search Keyword) |

### 2. Standard Paginated Response Structure (Page Wrapper DTO)
Response ของทุกข้อจะต้องถูกครอบด้วยโครงสร้าง JSON Envelope ดังนี้:

```json
{
  "content": [
    { /* ข้อมูล DTO ของแต่ละข้อ */ }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 23,
  "totalPages": 3,
  "isLast": false
}
```

---

## 🟢 ข้อที่ 1: ระดับ Simple (Employee Directory)

### 1.1 วัตถุประสงค์
ระบบต้องการ API ค้นหาและดู **"สมุดโทรศัพท์พนักงานภายในองค์กร"** โดยรองรับการแบ่งหน้า ค้นหาชื่อ/อีเมล และจัดเรียงข้อมูล

### 1.2 ข้อมูลต้นทาง (Source Entity)
ตาราง `employees`:
* `employeeNumber` (PK), `firstName`, `lastName`, `email`, `extension`, `officeCode`, `reportsTo`, `jobTitle`

### 1.3 ข้อกำหนด Pagination, Search & Sort
* **Search (`keyword`):**
  * ค้นหาแบบ Case-insensitive Partial Match (`LIKE %keyword%`) จากฟิลด์:
    * `firstName` หรือ `lastName` หรือ `email`
  * หากไม่ระบุ `keyword` ให้ดึงข้อมูลทั้งหมด
* **Sorting (`sortBy`, `sortDir`):**
  * ฟิลด์ที่อนุญาตให้จัดเรียง:
    * `id` (อ้างอิงจาก `employeeNumber`)
    * `fullName` (หรือ `firstName`)
    * `jobTitle`
  * **Default Sort:** เรียงตาม `id` แบบ `asc`
* **Pagination:**
  * รองรับ `page` และ `size` ตามมาตรฐานกลาง

### 1.4 โครงสร้าง DTO ของไอเทมใน `content`
```json
{
  "id": 1002,
  "fullName": "Diane Murphy",
  "email": "dmurphy@classicmodelcars.com",
  "phoneExtension": "x5800",
  "jobTitle": "President"
}
```

### 1.5 กฎทางธุรกิจและความปลอดภัย
1. `fullName` เกิดจาก `firstName + " " + lastName`
2. **Security:** ห้ามเปิดเผย `reportsTo` และ `officeCode` ใน Response เด็ดขาด

---

## 🟡 ข้อที่ 2: ระดับ Intermediate (Customer Sales Profile)

### 2.1 วัตถุประสงค์
ระบบ CRM ต้องการ API สำหรับค้นหาและแสดง **"ข้อมูลลูกค้าและรายละเอียดเซลส์ผู้ดูแล"** แบบ Flat Structure สำหรับแสดงบน Data Table ของหน้าเว็บ

### 2.2 ข้อมูลต้นทาง (Source Entities)
ความสัมพันธ์ 3 ตาราง:
* `Customer` (`customers`): `customerNumber`, `customerName`, `creditLimit`
* `Employee` (`employees`): `salesRep` (พนักงานขายที่ดูแล)
* `Office` (`offices`): `office` (สาขาของพนักงานขาย)

### 2.3 ข้อกำหนด Pagination, Search & Sort
* **Search (`keyword`) & Filter:**
  * `keyword`: ค้นหาจาก `customerName` หรือชื่อ-นามสกุลของเซลส์ผู้ดูแล (`salesRep`)
  * `country` *(Optional Filter)*: กรองเฉพาะลูกค้าที่เซลส์ประจำอยู่ในประเทศที่ระบุ (เช่น `?country=France`)
* **Sorting (`sortBy`, `sortDir`):**
  * ฟิลด์ที่อนุญาตให้จัดเรียง:
    * `customerId` (อ้างอิง `customerNumber`)
    * `customerName`
    * `creditLimit`
  * **Default Sort:** เรียงตาม `customerName` แบบ `asc`
* **Pagination:**
  * รองรับ `page` และ `size` ตามมาตรฐานกลาง

### 2.4 โครงสร้าง DTO ของไอเทมใน `content`
```json
{
  "customerId": 103,
  "customerName": "Atelier graphique",
  "creditLimit": 21000.00,
  "salesRepName": "Gerard Hernandez",
  "salesRepEmail": "ghernandez@classicmodelcars.com",
  "officeCity": "Paris",
  "officeCountry": "France"
}
```

### 2.5 กฎทางธุรกิจและ Null-Safety
1. **Object Flattening:** ยุบความสัมพันธ์ 3 ระดับ (`Customer` $\rightarrow$ `Employee` $\rightarrow$ `Office`) ให้อยู่ใน Object เดียว
2. **Null-Safety:** หากลูกค้าไม่มีเซลส์ดูแล (`salesRep == null`):
   * `salesRepName` ต้องแสดงเป็นข้อความ `"Unassigned"`
   * `salesRepEmail`, `officeCity`, และ `officeCountry` ต้องเป็น `null`
   * ห้ามเกิด `NullPointerException`

---

## 🔴 ข้อที่ 3: ระดับ Hard (Orders Summary & Deep Invoice)

### 3.1 วัตถุประสงค์
ระบบ E-Commerce ต้องการ API สองส่วน:
1. **Order List Endpoint (`GET /api/v1/orders`):** ค้นหาและแบ่งหน้ารายการสั่งซื้อทั้งหมด (แสดงยอดรวม `totalAmount` และจำนวนสินค้า `totalItems`)
2. **Order Detail Endpoint (`GET /api/v1/orders/{orderId}`):** แสดงรายละเอียดใบเสร็จฉบับเต็ม พร้อมรายการสินค้าและยอดคำนวณแต่ละรายการ

### 3.2 ข้อมูลต้นทาง (Source Entities)
* `Order` (`orders`): `orderNumber`, `orderDate`, `status`
* `Customer` (`customers`): `customerNumber`, `customerName`, `phone`
* `OrderDetail` (`orderdetails`): `quantityOrdered`, `priceEach`
* `Product` (`products`): `productCode`, `productName`

### 3.3 ข้อกำหนด Pagination, Search & Sort สำหรับ Order List Endpoint
* **Search / Filter:**
  * `status` *(Optional Filter)*: กรองตามสถานะ เช่น `"Shipped"`, `"In Process"`, `"Cancelled"`
  * `keyword` *(Optional Search)*: ค้นหาจากชื่อลูกค้า (`customer.customerName`)
* **Sorting (`sortBy`, `sortDir`):**
  * ฟิลด์ที่อนุญาตให้จัดเรียง:
    * `orderDate`
    * `orderId` (อ้างอิง `orderNumber`)
    * `status`
  * **Default Sort:** เรียงตาม `orderDate` แบบ `desc` (คำสั่งซื้อล่าสุดขึ้นก่อนเสมอ)
* **Pagination:**
  * รองรับ `page` และ `size` ตามมาตรฐานกลาง

### 3.4 โครงสร้าง DTO ของข้อที่ 3

#### A. โครงสร้างไอเทมใน `content` สำหรับ Order List (`GET /api/v1/orders`)
```json
{
  "orderId": 10100,
  "orderDate": "2003-01-06",
  "status": "Shipped",
  "customerName": "Online Diecast Creations Co.",
  "totalItems": 4,
  "totalAmount": 10223.83
}
```

#### B. โครงสร้างสำหรับ Order Detail ฉบับเต็ม (`GET /api/v1/orders/{orderId}`)
```json
{
  "orderId": 10100,
  "orderDate": "2003-01-06",
  "status": "Shipped",
  "customer": {
    "customerId": 363,
    "customerName": "Online Diecast Creations Co.",
    "phone": "6035557580"
  },
  "items": [
    {
      "productCode": "S18_1749",
      "productName": "1917 Grand Touring Car",
      "quantity": 30,
      "unitPrice": 136.00,
      "subtotal": 4080.00
    },
    {
      "productCode": "S18_2248",
      "productName": "1911 Ford Town Car",
      "quantity": 50,
      "unitPrice": 55.09,
      "subtotal": 2754.50
    }
  ],
  "totalAmount": 6834.50
}
```

### 3.5 กฎทางธุรกิจและการคำนวณ
1. **Calculated Field 1 (ระดับ Item):**
   $$\text{subtotal} = \text{quantityOrdered} \times \text{priceEach}$$
2. **Calculated Field 2 (ระดับ Order Summary):**
   $$\text{totalAmount} = \sum (\text{subtotal ของทุก item})$$
   $$\text{totalItems} = \sum (\text{quantityOrdered ทั้งหมด หรือจำนวนรายการสินค้าใน order})$$
3. **Data Integrity & Edge Cases:**
   * ใช้ `BigDecimal` สำหรับยอดเงินทั้งหมดเพื่อป้องกันความคลาดเคลื่อนของทศนิยม
   * กรณี Order ไม่มีสินค้า: `items` ต้องเป็น `[]` และ `totalAmount` ต้องเป็น `0.00`

---

## 📊 เกณฑ์การประเมินและให้คะแนน (Grading Rubric - ปรับปรุงใหม่)

| หัวข้อการประเมิน | สัดส่วน | รายละเอียดการพิจารณา |
| :--- | :---: | :--- |
| **1. โครงสร้าง DTO & Response Envelope (25%)** | 25 คะแนน | โครงสร้าง Page Wrapper (`content`, `totalElements`, etc.) และ JSON DTO ถูกต้องตาม Spec ครบทั้ง 3 ข้อ |
| **2. ระบบค้นหา กรอง และจัดเรียง (Search & Sort) (25%)** | 25 คะแนน | ฟังก์ชันค้นหา Partial match ทำงานถูกต้อง, จัดเรียงตามฟิลด์ที่กำหนดได้ทั้ง ASC/DESC และมี Default sort ที่ถูกต้อง |
| **3. Pagination & Performance (20%)** | 20 คะแนน | การตัดหน้า (`page`, `size`) ทำงานถูกต้องที่ระดับ Database (ไม่ดึงข้อมูลทั้งหมดมาตัดบน Memory) |
| **4. ความถูกต้องของการคำนวณและ Null-Safety (15%)** | 15 คะแนน | คำนวณ `subtotal`, `totalAmount` แม่นยำ และรองรับกรณีฟิลด์เป็น `null` ได้อย่างปลอดภัย |
| **5. การออกแบบสถาปัตยกรรมและ Clean Code (15%)** | 15 คะแนน | แยก Layer ชัดเจน (Controller $\leftrightarrow$ Service $\leftrightarrow$ Repository), โค้ดสะอาด เข้าใจง่าย |
