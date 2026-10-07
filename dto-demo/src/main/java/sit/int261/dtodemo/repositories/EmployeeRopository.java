package sit.int261.dtodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.dtodemo.entities.Employee;

public interface EmployeeRopository extends JpaRepository<Employee, Integer> {
}
