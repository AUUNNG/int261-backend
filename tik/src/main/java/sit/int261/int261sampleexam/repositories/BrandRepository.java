package sit.int261.int261sampleexam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.int261sampleexam.entities.Brand;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findAllByOrderByIdAsc();
}
