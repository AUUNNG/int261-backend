package sit.int261.controllerdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.controllerdemo.entities.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
