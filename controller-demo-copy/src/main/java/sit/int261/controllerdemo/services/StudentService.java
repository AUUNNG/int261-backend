package sit.int261.controllerdemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.controllerdemo.entities.Course;
import sit.int261.controllerdemo.entities.Student;
import sit.int261.controllerdemo.entities.Subject;
import sit.int261.controllerdemo.repositories.CourseRepository;
import sit.int261.controllerdemo.repositories.StudentRepository;
import sit.int261.controllerdemo.repositories.SubjectRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private SubjectRepository subjectRepo;

    public List<Student> findAll() {
        return studentRepo.findAll();
    }

    public Page<Student> findAll(String name, List<Long> ids, Pageable pageable) {
        Specification<Student> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }

            if (ids != null && !ids.isEmpty()) {
                predicates.add(root.get("id").in(ids));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };

        return studentRepo.findAll(spec, pageable);
    }

    public Student findById(Long id) {
        return studentRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found for id: " + id)
        );
    }

    public Course addCourse(Long studentId, Course course) {
        return addCourses(studentId, List.of(course)).get(0);
    }

    public List<Course> addCourses(Long studentId, List<Course> courses) {
        Student student = findById(studentId);

        if (courses == null || courses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course list cannot be empty");
        }

        for (Course course : courses) {
            if (course.getSubject() == null || course.getSubject().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject id is required for all courses");
            }

            Subject subject = subjectRepo.findById(course.getSubject().getId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject with id: " + course.getSubject().getId() + " not found")
            );

            course.setStudent(student);
            course.setSubject(subject);
        }

        return courseRepo.saveAll(courses);
    }
}
