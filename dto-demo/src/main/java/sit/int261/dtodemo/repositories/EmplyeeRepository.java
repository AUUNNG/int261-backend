package sit.int261.dtodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.dtodemo.entities.Employee;

public interface EmplyeeRepository extends JpaRepository<Employee, Integer> {
}
