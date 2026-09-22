package sit.int261.controllerdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.controllerdemo.entities.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
}
