package sit.int261.exceptiondemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.exceptiondemo.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
