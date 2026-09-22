package sit.int261.controllerdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.controllerdemo.entities.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
