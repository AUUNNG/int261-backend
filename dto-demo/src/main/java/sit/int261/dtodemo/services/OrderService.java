package sit.int261.dtodemo.services;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.dtodemo.dtos.*;
import sit.int261.dtodemo.entities.Customer;
import sit.int261.dtodemo.entities.Order;
import sit.int261.dtodemo.entities.OrderDetail;
import sit.int261.dtodemo.repositories.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryDto> getOrders(Integer page, Integer size, String sortBy, String sortDir, String status, String keyword) {
        int pageNumber = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size <= 0) ? 10 : size;

        // Default sort: orderDate desc
        Sort.Direction direction;
        if (sortDir != null && "asc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }

        Sort sort;
        if ("orderId".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(direction, "id");
        } else if ("status".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(direction, "status");
        } else {
            // Default sort: orderDate desc
            sort = Sort.by(direction, "orderDate");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.trim().toLowerCase()));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                Join<Order, Customer> customer = root.join("customer", JoinType.LEFT);
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(customer.get("customerName")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderSummaryDto> content = orderPage.getContent().stream()
                .map(this::toOrderSummaryDto)
                .toList();

        return PageResponse.from(orderPage, content);
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order with id " + orderId + " not found"
                )
        );

        Customer cust = order.getCustomer();
        CustomerBasicDto customerDto = null;
        if (cust != null) {
            customerDto = CustomerBasicDto.builder()
                    .customerId(cust.getId())
                    .customerName(cust.getCustomerName())
                    .phone(cust.getPhone())
                    .build();
        }

        List<OrderItemDto> itemDtos = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                BigDecimal unitPrice = detail.getPriceEach() != null
                        ? detail.getPriceEach().setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                int qty = detail.getQuantityOrdered() != null ? detail.getQuantityOrdered() : 0;
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

                totalAmount = totalAmount.add(subtotal);

                itemDtos.add(OrderItemDto.builder()
                        .productCode(detail.getProduct() != null ? detail.getProduct().getProductCode() : (detail.getId() != null ? detail.getId().getProductCode() : null))
                        .productName(detail.getProduct() != null ? detail.getProduct().getProductName() : null)
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .subtotal(subtotal)
                        .build());
            }
        }

        return OrderDetailDto.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .customer(customerDto)
                .items(itemDtos)
                .totalAmount(totalAmount)
                .build();
    }

    private OrderSummaryDto toOrderSummaryDto(Order order) {
        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int totalItems = 0;

        if (order.getOrderDetails() != null) {
            totalItems = order.getOrderDetails().size();
            for (OrderDetail detail : order.getOrderDetails()) {
                BigDecimal unitPrice = detail.getPriceEach() != null
                        ? detail.getPriceEach()
                        : BigDecimal.ZERO;
                int qty = detail.getQuantityOrdered() != null ? detail.getQuantityOrdered() : 0;
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                totalAmount = totalAmount.add(subtotal);
            }
        }

        return OrderSummaryDto.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .customerName(order.getCustomer() != null ? order.getCustomer().getCustomerName() : null)
                .totalItems(totalItems)
                .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
