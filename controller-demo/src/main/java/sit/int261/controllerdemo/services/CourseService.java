package sit.int261.controllerdemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.controllerdemo.entities.Course;
import sit.int261.controllerdemo.entities.Student;
import sit.int261.controllerdemo.repositories.CourseRepository;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepository repo;

    public List<Course> findAll() {
        return repo.findAll();
    }

    public Course findById(Long id) {
        Course course = repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course with id: " + id + " not found!")
        );
        return course;
    }
}
