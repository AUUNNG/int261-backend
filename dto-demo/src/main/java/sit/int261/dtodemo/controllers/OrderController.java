package sit.int261.dtodemo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int261.dtodemo.dtos.OrderDetailDto;
import sit.int261.dtodemo.dtos.OrderSummaryDto;
import sit.int261.dtodemo.dtos.PageResponse;
import sit.int261.dtodemo.services.OrderService;

@RestController
@RequestMapping({"/api/v1/orders", "/orders"})
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryDto>> getOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<OrderSummaryDto> response = orderService.getOrders(page, size, sortBy, sortDir, status, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailDto> getOrderDetail(@PathVariable Integer orderId) {
        OrderDetailDto response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }
}
