package sit.int261.controllerdemo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int261.controllerdemo.entities.Course;
import sit.int261.controllerdemo.entities.Student;
import sit.int261.controllerdemo.services.StudentService;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @GetMapping
    public List<Student> getStudents() {
        return studentService.findAll();
    }

    @GetMapping("/page")
    public Page<Student> getStudentsPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> ids,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return studentService.findAll(name, ids, pageable);
    }

    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<List<Course>> getStudentCourse(@PathVariable Long id) {
        Student student = studentService.findById(id);
        return ResponseEntity.ok(student.getCourses());
    }

    @PostMapping("/{id}/courses")
    public ResponseEntity<List<Course>> createStudentCourses(@PathVariable Long id, @RequestBody List<Course> newCourses) {
        List<Course> created = studentService.addCourses(id, newCourses);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
