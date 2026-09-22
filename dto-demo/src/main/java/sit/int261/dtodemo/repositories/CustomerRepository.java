package sit.int261.dtodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.dtodemo.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
