package sit.int261.dtodemo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.dtodemo.controllers.CustomerController;
import sit.int261.dtodemo.controllers.EmployeeController;
import sit.int261.dtodemo.controllers.OrderController;
import sit.int261.dtodemo.dtos.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExercisesIntegrationTest {

    @Autowired
    private EmployeeController employeeController;

    @Autowired
    private CustomerController customerController;

    @Autowired
    private OrderController orderController;

    @Test
    @DisplayName("Q1: Employee Directory - Pagination & Envelope")
    void testQuestion1_PaginationAndEnvelope() {
        ResponseEntity<PageResponse<EmployeeSummaryDto>> res = employeeController.getEmployees(0, 10, "id", "asc", null);
        assertNotNull(res);
        PageResponse<EmployeeSummaryDto> page = res.getBody();
        assertNotNull(page);

        assertEquals(0, page.getPageNumber());
        assertEquals(10, page.getPageSize());
        assertEquals(23L, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertFalse(page.getIsLast());

        List<EmployeeSummaryDto> content = page.getContent();
        assertEquals(10, content.size());

        EmployeeSummaryDto first = content.get(0);
        assertNotNull(first.getId());
        assertNotNull(first.getFullName());
        assertNotNull(first.getEmail());
        assertNotNull(first.getPhoneExtension());
        assertNotNull(first.getJobTitle());
    }

    @Test
    @DisplayName("Q1: Employee Directory - Search & Sort")
    void testQuestion1_SearchAndSort() {
        ResponseEntity<PageResponse<EmployeeSummaryDto>> searchRes = employeeController.getEmployees(0, 10, "id", "asc", "diane");
        PageResponse<EmployeeSummaryDto> searchPage = searchRes.getBody();
        assertNotNull(searchPage);
        assertFalse(searchPage.getContent().isEmpty());
        assertTrue(searchPage.getContent().get(0).getFullName().toLowerCase().contains("diane"));

        // Sort by jobTitle desc
        ResponseEntity<PageResponse<EmployeeSummaryDto>> sortRes = employeeController.getEmployees(0, 10, "jobTitle", "desc", null);
        PageResponse<EmployeeSummaryDto> sortPage = sortRes.getBody();
        assertNotNull(sortPage);
        assertEquals(10, sortPage.getContent().size());
    }

    @Test
    @DisplayName("Q2: Customer Sales Profile - Flat Structure & Null Safety")
    void testQuestion2_CustomerSalesProfile() {
        ResponseEntity<PageResponse<CustomerSalesProfileDto>> res = customerController.getCustomers(0, 20, "customerName", "asc", null, null);
        assertNotNull(res);
        PageResponse<CustomerSalesProfileDto> page = res.getBody();
        assertNotNull(page);

        assertEquals(122L, page.getTotalElements());
        assertFalse(page.getContent().isEmpty());

        CustomerSalesProfileDto first = page.getContent().get(0);
        assertNotNull(first.getCustomerId());
        assertNotNull(first.getCustomerName());

        // Check unassigned sales rep null-safety
        CustomerSalesProfileDto unassignedCust = page.getContent().stream()
                .filter(c -> "Unassigned".equals(c.getSalesRepName()))
                .findFirst()
                .orElse(null);

        assertNotNull(unassignedCust, "Should find customer with Unassigned sales reps");
        assertNull(unassignedCust.getSalesRepEmail());
        assertNull(unassignedCust.getOfficeCity());
        assertNull(unassignedCust.getOfficeCountry());
    }

    @Test
    @DisplayName("Q2: Customer Filter by country")
    void testQuestion2_FilterByCountry() {
        ResponseEntity<PageResponse<CustomerSalesProfileDto>> res = customerController.getCustomers(0, 20, "customerName", "asc", null, "France");
        assertNotNull(res);
        PageResponse<CustomerSalesProfileDto> page = res.getBody();
        assertNotNull(page);

        assertFalse(page.getContent().isEmpty());
        for (CustomerSalesProfileDto item : page.getContent()) {
            assertEquals("France", item.getOfficeCountry());
        }
    }

    @Test
    @DisplayName("Q3: Order List Summary")
    void testQuestion3_OrderListSummary() {
        ResponseEntity<PageResponse<OrderSummaryDto>> res = orderController.getOrders(0, 10, "orderDate", "desc", null, null);
        assertNotNull(res);
        PageResponse<OrderSummaryDto> page = res.getBody();
        assertNotNull(page);

        assertEquals(326L, page.getTotalElements());
        assertEquals(10, page.getContent().size());

        OrderSummaryDto first = page.getContent().get(0);
        assertNotNull(first.getOrderId());
        assertNotNull(first.getOrderDate());
        assertNotNull(first.getStatus());
        assertNotNull(first.getCustomerName());
        assertNotNull(first.getTotalItems());
        assertNotNull(first.getTotalAmount());
    }

    @Test
    @DisplayName("Q3: Order Detail Deep Invoice (Order 10100)")
    void testQuestion3_OrderDetail() {
        ResponseEntity<OrderDetailDto> res = orderController.getOrderDetail(10100);
        assertNotNull(res);
        OrderDetailDto detail = res.getBody();
        assertNotNull(detail);

        assertEquals(10100, detail.getOrderId());
        assertEquals("Shipped", detail.getStatus());

        assertNotNull(detail.getCustomer());
        assertEquals("Online Diecast Creations Co.", detail.getCustomer().getCustomerName());

        assertNotNull(detail.getItems());
        assertEquals(4, detail.getItems().size());

        // Verify total amount = 10223.83
        assertEquals(new BigDecimal("10223.83"), detail.getTotalAmount());

        // Verify individual item calculation: subtotal = quantity * unitPrice
        for (OrderItemDto item : detail.getItems()) {
            assertNotNull(item.getProductCode());
            assertNotNull(item.getProductName());
            BigDecimal expectedSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            assertEquals(expectedSubtotal, item.getSubtotal());
        }
    }

    @Test
    @DisplayName("Q3: Order Detail 404 for non-existent order")
    void testQuestion3_NotFound() {
        assertThrows(ResponseStatusException.class, () -> orderController.getOrderDetail(999999));
    }
}
