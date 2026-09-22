package sit.int261.controllerdemo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.int261.controllerdemo.entities.Course;
import sit.int261.controllerdemo.entities.Student;
import sit.int261.controllerdemo.services.CourseService;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Course>> getCourses(){
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping({"/{id}/student", "/{id}/students"})
    public ResponseEntity<Student> getStudentCourse(@PathVariable Long id) {
        Course course = courseService.findById(id);
        Student student = course.getStudent();
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }
}
