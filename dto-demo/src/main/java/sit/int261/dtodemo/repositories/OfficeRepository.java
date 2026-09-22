package sit.int261.dtodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.dtodemo.entities.Office;

public interface OfficeRepository extends JpaRepository<Office, String> {
}
