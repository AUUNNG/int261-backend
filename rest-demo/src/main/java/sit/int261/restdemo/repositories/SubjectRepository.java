package sit.int261.restdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.restdemo.entities.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
}