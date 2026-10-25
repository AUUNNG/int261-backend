package sit.int261.int261sampleexam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.int261sampleexam.entities.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByIdAsc();
    List<Product> findByBrandIdOrderByIdAsc(Long brandId);

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
}
