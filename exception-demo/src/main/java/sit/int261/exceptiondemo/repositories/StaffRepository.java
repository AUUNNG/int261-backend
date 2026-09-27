package sit.int261.exceptiondemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.exceptiondemo.entities.Staff;

public interface StaffRepository extends JpaRepository<Staff, Short> {
}
