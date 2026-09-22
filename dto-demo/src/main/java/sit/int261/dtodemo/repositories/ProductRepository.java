package sit.int261.dtodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.dtodemo.entities.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
}
