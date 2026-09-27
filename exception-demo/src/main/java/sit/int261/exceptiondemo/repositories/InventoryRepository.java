package sit.int261.exceptiondemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.exceptiondemo.entities.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
}
