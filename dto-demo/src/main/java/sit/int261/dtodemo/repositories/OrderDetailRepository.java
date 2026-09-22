package sit.int261.dtodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int261.dtodemo.entities.OrderDetail;
import sit.int261.dtodemo.entities.OrderDetailId;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {
}
