package sit.int261.exceptiondemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.exceptiondemo.entities.Rental;

public interface RentalRepository extends JpaRepository<Rental, Integer> {
    boolean existsByInventoryIdAndReturnDateIsNull(Integer inventoryId);
}
