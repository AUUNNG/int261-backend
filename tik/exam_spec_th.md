# ข้อสอบภาคปฏิบัติ: Mobile Product REST API ด้วย Spring Boot

## 1. สถานการณ์และวัตถุประสงค์
ร้าน Mobile Hub ต้องการระบบจัดการสินค้าโทรศัพท์มือถือและแบรนด์ ให้นักศึกษาพัฒนา RESTful API ที่บันทึกข้อมูลจริงในฐานข้อมูล ครอบคลุม REST principles, CRUD, DTO และ Exception handling ระบบจะถูกตรวจจาก HTTP request/response และตรวจโครงสร้าง source code แยกกัน


## 2. สภาพแวดล้อมและขอบเขต
ผู้สอนต้องแจก starter project และตรึง Spring Boot patch version เดียวกันก่อนสอบ แนะนำ Java 21, Spring MVC, Spring Data JPA, Bean Validation, Maven Wrapper และ MySQL 8.0 ขึ้นไป ใช้ jakarta.* ตาม starter ไม่ให้นักศึกษาเลือกหรืออัปเกรด version เองระหว่างสอบ

ใช้ฐานข้อมูล mobile_exam ที่ให้มาและชื่อ table/column ตาม SQL ไม่ใช้ข้อมูลใน List/Map แทนฐานข้อมูล ไม่สร้าง schema ใหม่ด้วย Hibernate: ใช้ spring.jpa.hibernate.ddl-auto=validate การเปลี่ยนแปลงต้องคงอยู่หลัง restart แอป

ฐาน URL: http://localhost:8080/api/v1 ทุกคำขอเป็นอิสระและไม่พึ่ง session ข้อสอบไม่รวม authentication, pagination, PATCH, file upload, frontend, HATEOAS หรือ CRUD ของ brands ทั้งหมด รับ/ส่ง JSON ยกเว้น DELETE สำเร็จที่ไม่มี body

GET ต้องไม่แก้ไขฐานข้อมูล PUT เป็นการแทนค่าทุก field ที่เขียนได้ ไม่ใช่ partial update DELETE ซ้ำสามารถให้ 404 ได้ เพราะ idempotent หมายถึงผลต่อสถานะระบบซ้ำแล้วไม่เปลี่ยน ไม่ได้บังคับ status code เท่ากันทุกครั้ง

## 3. โครงสร้างฐานข้อมูล
brands: id BIGINT PK auto increment, name VARCHAR(60) unique, country VARCHAR(60)
products: id BIGINT PK auto increment, sku VARCHAR(30) unique, name VARCHAR(100), brand_id FK brands.id, price DECIMAL(10,2), stock INT, ram_gb INT, storage_gb INT, color VARCHAR(40), description VARCHAR(500) nullable, active BOOLEAN

ความสัมพันธ์ Brand 1:N Product; product ต้องมี brand เสมอ FK ใช้ RESTRICT เมื่อพยายามลบ brand ที่มี product ราคาหน่วยบาท stock หน่วยเครื่อง RAM/storage หน่วย GB ไม่เก็บ floating point สำหรับราคา

Seed มี 8 brands และ 48 products IDs 1–48 พร้อมค่าชายขอบ เช่น stock=0, price=0.01 และ 99999999.99, description=null/ว่าง, active=false และข้อความไทย Brand 8 ไม่มีสินค้า ใช้ทดสอบ filter ที่ต้องได้ []

## 4. DTO และสัญญา JSON
ต้องมี ProductRequest, ProductResponse, BrandResponse และ ErrorResponse หรือชื่อเทียบเท่า แยก DTO ออกจาก JPA Entity ห้ามรับ/ส่ง Entity ผ่าน controller โดยตรง ใช้ request DTO เดียวกันสำหรับ POST และ PUT ได้

ProductRequest มี field ต่อไปนี้ โดย POST และ PUT ต้องส่งทุก field ที่ required ไม่ใช้ค่า default แทน field ที่ขาด:

| Field | Type | Required | เงื่อนไข |
|---|---|---|---|
| sku | string | ใช่ | trim ASCII space ก่อนตรวจ; 3–30 ตัวอักษร; pattern [A-Z0-9-]+; ต้องไม่ซ้ำ |
| name | string | ใช่ | trim ASCII space; 1–100 ตัวอักษรหลัง trim; ห้ามว่าง |
| brandId | integer | ใช่ | >0 และมีอยู่จริง |
| price | number | ใช่ | 0.01–99999999.99 รวมขอบ; ทศนิยมไม่เกิน 2 ตำแหน่ง |
| stock | integer | ใช่ | 0–100000 รวมขอบ |
| ramGb | integer | ใช่ | 2, 4, 6, 8, 12, 16 หรือ 24 เท่านั้น |
| storageGb | integer | ใช่ | 32, 64, 128, 256, 512 หรือ 1024 เท่านั้น |
| color | string | ใช่ | trim ASCII space; 1–40 ตัวอักษร; ห้ามว่าง |
| description | string/null | ไม่ | null/ขาด field = null; ยาวไม่เกิน 500; ไม่ trim |
| active | boolean | ใช่ | ต้องเป็น JSON true/false ไม่รับ string/number/null |

id, brand object และ field อื่นที่ไม่ได้ระบุไม่อนุญาตใน request: คืน 400 INVALID_REQUEST ทุก numeric field ไม่รับ string, boolean, object/array; field integer ไม่รับทศนิยมแม้ 1.0 และไม่รับเลขที่เกินช่วงชนิดข้อมูล String field ไม่รับการแปลงชนิดอัตโนมัติ เช่น name=123 ต้อง 400 INVALID_REQUEST ข้อมูล JSON ผิดชนิดหรือ deserialize ไม่ได้เป็น INVALID_REQUEST; deserialize ได้แต่ไม่ผ่านเงื่อนไขเป็น VALIDATION_ERROR ต้องตั้งค่า Jackson หรือเขียน validation ให้สอดคล้องกัน

ProductResponse ต้องมี key เท่านี้ ไม่มี field เพิ่ม: id, sku, name, brand, price, stock, ramGb, storageGb, color, description, active โดย brand เป็น {"id":1,"name":"Apple"} ไม่มี brandId ที่ root หรือ country ใน brand ของ product; description ต้องส่ง key แม้เป็น null ราคาส่ง JSON number; การตรวจจะเปรียบเทียบค่าเชิงตัวเลข ไม่บังคับ 100.00 เทียบกับ 100.0

BrandResponse สำหรับ GET /brands เป็น {"id":1,"name":"Apple","country":"USA"} เท่านั้น ไม่ส่ง products และไม่มี reference วนซ้ำ ไม่ส่งค่า internal ของ persistence provider

ตัวอย่าง request ที่ถูกต้อง:
```json
{"sku":"EXAM-001","name":"Exam Phone","brandId":1,"price":15990.50,"stock":10,"ramGb":8,"storageGb":256,"color":"Black","description":"เครื่องทดสอบ","active":true}
```
ตัวอย่าง response (id เป็นเพียงตัวอย่าง; ไม่บังคับ id ใหม่ต้องเป็น 49):
```json
{"id":49,"sku":"EXAM-001","name":"Exam Phone","brand":{"id":1,"name":"Apple"},"price":15990.50,"stock":10,"ramGb":8,"storageGb":256,"color":"Black","description":"เครื่องทดสอบ","active":true}
```

## 5. Endpoint ที่ต้องพัฒนา
| Method / Path | พฤติกรรม | สำเร็จ |
|---|---|---|
| GET /brands | ทุกแบรนด์ เรียง id ASC | 200 array |
| GET /products | ทุกสินค้า เรียง id ASC รวม active=false | 200 array |
| GET /products?brandId=1 | กรองแบรนด์ เรียง id ASC | 200 array |
| GET /products/{id} | อ่านสินค้ารายตัว | 200 ProductResponse |
| POST /products | สร้างสินค้า | 201 ProductResponse + Location |
| PUT /products/{id} | แทนค่าทุก field ที่เขียนได้ | 200 ProductResponse |
| DELETE /products/{id} | ลบจริงจากฐานข้อมูล | 204 ไม่มี body |

POST ต้องสร้าง id จากฐานข้อมูลและคืน Location ที่ชี้ /api/v1/products/{newId} จะเป็น relative URI หรือ absolute URI ของบริการก็ได้ GET ตาม Location ต้องพบข้อมูลที่เพิ่งสร้าง ห้าม hardcode id หรือใช้ id จาก client

PUT ไม่เปลี่ยน id และไม่สร้างสินค้าเมื่อ id ไม่พบ สามารถเปลี่ยน brand/sku ได้ ส่ง sku เดิมของตัวเองได้ แต่ห้าม sku ของสินค้าอื่น การเรียก PUT ด้วย body เดิมซ้ำต้องให้ข้อมูลเดิมและไม่เพิ่มจำนวนสินค้า description ที่ขาดใน PUT ต้องกลายเป็น null

GET filter: brandId ไม่ส่ง = ทุกสินค้า; brandId เป็น brand ที่มีอยู่แต่ไม่มีสินค้า = []; brandId บวกแต่ไม่มี brand = 404 BRAND_NOT_FOUND; ค่าว่าง, ไม่ใช่ integer, <=0, ซ้ำหลายค่า, เกิน BIGINT = 400 INVALID_REQUEST; query parameter อื่น = 400 INVALID_REQUEST ห้าม silently ignore

Path id ต้องเป็น positive integer แบบ decimal digits เท่านั้น ไม่รับ +1, 1.0, 0, -1, abc หรือ overflow: 400 INVALID_REQUEST ค่า leading zero เช่น 001 อนุญาตและหมายถึง id=1 ไม่รองรับ trailing slash และ route อื่นไม่อยู่ในคะแนนนี้ GET /brands และ endpoint อื่นไม่รับ query parameter

## 6. Exception handling
ต้องมี global exception handler เช่น @RestControllerAdvice และ custom exception ที่สื่อความหมาย แยก error business กับ validation ไม่ return 200 เมื่อเกิด error ไม่ส่ง stack trace, SQL, class name, exception name หรือข้อมูล secret

รูปแบบ error ทุกกรณีในตารางด้านล่างต้องมี key เท่านี้:
```json
{"status":400,"code":"VALIDATION_ERROR","message":"Invalid product data","path":"/api/v1/products","fieldErrors":[{"field":"price","message":"must be between 0.01 and 99999999.99"}]}
```
status เป็น integer ตรงกับ HTTP status; path เป็น request path ไม่รวม query string; message เป็นข้อความที่ไม่ว่าง เลือกไทยหรืออังกฤษได้ ไม่ตรวจ exact text; fieldErrors เป็น array เสมอ แต่ละสมาชิกมี field และ message เท่านั้น

| สถานการณ์ | HTTP | code | fieldErrors |
|---|---|---|---|
| body ผ่านการแปลงชนิดแต่ผิดข้อกำหนด | 400 | VALIDATION_ERROR | มีทุก field ที่ผิด; แต่ละ field อย่างน้อย 1 รายการ |
| JSON malformed, body ว่าง, field ชนิดผิด, unknown field, invalid path/query | 400 | INVALID_REQUEST | [] |
| product ไม่พบใน GET/PUT/DELETE | 404 | PRODUCT_NOT_FOUND | [] |
| brand ไม่พบใน POST/PUT/filter | 404 | BRAND_NOT_FOUND | [] |
| sku ซ้ำกับสินค้ารายอื่น | 409 | DUPLICATE_SKU | [] |
| media type ของ POST/PUT ไม่ใช่ application/json | 415 | UNSUPPORTED_MEDIA_TYPE | [] |
| POST/PUT ส่ง Accept: text/plain | 406 | NOT_ACCEPTABLE | [] |
| POST /brands ซึ่งไม่มี handler สำหรับ method นี้ | 405 | METHOD_NOT_ALLOWED | [] |
| เหตุขัดข้องภายในที่ไม่คาดคิด | 500 | INTERNAL_ERROR | [] |

error response เป็น application/json รวมกรณี 406 ของข้อสอบนี้ ตรวจ Accept ก่อน mutation เพื่อให้คำขอที่ได้ 406 ไม่สร้าง/เปลี่ยนข้อมูล ผู้สอนตรวจ 500 จาก code review/controlled fault injection เท่านั้น ไม่บังคับสร้าง endpoint สำหรับทำให้ระบบล้ม

ลำดับตรวจเมื่อผิดหลายอย่าง: unsupported Content-Type → unacceptable Accept → path/query/body parse/type/unknown fields → body validation → product exists (PUT/DELETE) → brand exists → SKU uniqueness → write โดย tester จะส่ง Content-Type และ Accept ที่ปกติยกเว้นกรณีทดสอบเฉพาะ ไม่ทดสอบ combination ของปัญหา transport หลายรายการ

VALIDATION_ERROR ต้องรวมทุก field ที่ตรวจพบ ไม่มีข้อกำหนดลำดับ fieldErrors หรือข้อความตรงตัว ข้อมูลที่ไม่ผ่านต้องไม่เปลี่ยนฐานข้อมูล แม้ PUT พบว่า brand/sku ใช้ไม่ได้ ต้อง rollback/ไม่ save ข้อมูลบางส่วน จัดการ unique constraint violation เป็น 409 ไม่ปล่อย 500

## 7. โครงสร้างโปรแกรมที่ต้องส่ง
แยก controller → service → repository; business logic อยู่ service; repository เข้าถึงฐานข้อมูล; DTO mapping ทำใน service/mapper ได้ ใช้ constructor injection และ BigDecimal สำหรับราคา มี transaction สำหรับการเขียนข้อมูล ไม่จำเป็นต้องใช้ MapStruct/Lombok และไม่บังคับชื่อ package/class ของนักศึกษา

ต้องส่ง source code, pom.xml, Maven Wrapper, README วิธีตั้งค่าฐานข้อมูลและรันแอป ผู้สอนจัดเตรียมค่าการเชื่อมต่อผ่าน environment variables ห้ามแนบ password จริงหรือ binary build แทน source ไม่ต้องส่ง frontend หรือเฉลย test ผู้สอน

ตัวอย่าง config:
```properties
server.port=${SERVER_PORT:8080}
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/mobile_exam}
spring.datasource.username=${DB_USER:exam}
spring.datasource.password=${DB_PASSWORD:exam}
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
```
ผู้สอนให้ user exam เข้าถึงเฉพาะ mobile_exam ตามสภาพแวดล้อมห้องสอบ SQL import ต้องทำก่อนเริ่มแอป ไม่ใช้ data.sql seed ซ้ำทุก restart

## 8. เกณฑ์คะแนน 100 คะแนน
| หมวด | คะแนน | หลักฐาน |
|---|---:|---|
| REST principles | 15 | resource/method/status 8; Location 3; GET ไม่ mutate + idempotence PUT/DELETE 4 |
| CRUD + persistence | 35 | list/brands 6; get 5; create 8; update 8; delete 5; filter 3 |
| DTO | 20 | request/response แยก Entity 5; shape/nested brand/null 7; validation/normalization/type 8 |
| Exception handling | 20 | error schema 4; 400 5; 404 4; 409 3; 405/415/406 2; 500 ไม่รั่วข้อมูล 2 |
| โครงสร้าง/รันได้ | 10 | layers/injection 4; transaction/BigDecimal/constraint handling 3; README/config/build 3 |

คะแนนแต่ละพฤติกรรมให้ตามหลักฐาน ไม่หักซ้ำในหมวดเดียวกันจากข้อผิดพลาดต้นเหตุเดียว Source code review จำเป็น: black-box test ยืนยันการแยก DTO/layer/transaction ทั้งหมดไม่ได้ ไม่ควรให้คะแนนอัตโนมัติจากจำนวน tests ผ่านอย่างเดียว

## 9. ตัวอย่างลำดับตรวจภาคปฏิบัติ
1. Reset seed: 8 brands, 48 products; GET ทั้งหมดเท่ากับไฟล์ expected_products.json ตาม id
2. GET id=1 ตรงข้อมูล seed; GET id=999999 →404
3. POST valid →201; อ่าน id จาก response/Location และ GET ตรวจ; จำนวนเพิ่ม 1
4. POST sku เดิม →409; จำนวนไม่เพิ่ม
5. PUT เปลี่ยนราคา/stock/brand →200; GET ตรวจครบ; PUT ซ้ำไม่เพิ่ม row
6. PUT body ขาด stock →400; เปรียบเทียบก่อน/หลังต้องไม่เปลี่ยน
7. PUT ใช้ brand ไม่มี/sku ของ row อื่น →404/409; ค่าเดิมไม่เปลี่ยน
8. DELETE row ที่สร้าง →204 และไม่มี body; GET/DELETE ซ้ำ →404
9. ตรวจ boundary valid/invalid และ unknown field/type coercion
10. Restart แอปโดยไม่ reset แล้ว GET ค่า update เพื่อตรวจ persistence; คืน seed ก่อนตรวจคนถัดไป

## 10. ข้อกำหนดสำหรับผู้สอนและระบบทดสอบ
แยกชุดแจกนักศึกษา (spec, schema/seed, CSV) จากชุดผู้สอน (pytest, expected JSON, grading notes) ห้ามแจก teacher.zip หรือชุดรวมที่มี test หากตั้งใจเก็บ test เป็น hidden

ใช้ฐานข้อมูลแยกต่อผู้เข้าสอบและรัน tests แบบ sequential ไม่รัน pytest-xdist เพราะ tests มี mutation ก่อนตรวจแต่ละ submission ต้องหยุดแอป, import reset.sql, เริ่มแอป, รอ GET /brands พร้อมใช้งาน แล้วรัน tests ห้าม reset ระหว่าง test suite; tests เขียนข้อมูลใหม่ด้วย SKU prefix TST- และไม่ใช้ id ที่คาดเดา การ cleanup ต้องทำแม้ test fail

ชุด pytest ที่แนบเป็น black-box starter สำหรับผู้สอน ไม่ใช่เฉลยแอป และไม่ครอบคลุมทุก requirement ของข้อสอบ test cases เชิงโครงสร้าง, restart persistence, internal error และ concurrency ต้องตรวจเพิ่มตาม teacher_notes.md ผู้สอนต้องทดลอง suite กับ reference implementation ก่อนใช้ให้คะแนนจริง

## 11. เอกสารอ้างอิง
ข้อกำหนดเชิง HTTP อ้างอิง RFC 9110 แต่ schema DTO, code ของ error, การ trim, strict typing และลำดับ validation เป็นสัญญาเฉพาะข้อสอบ ไม่ใช่สิ่งที่ REST บังคับทั้งหมด
- https://www.rfc-editor.org/rfc/rfc9110.html
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html
