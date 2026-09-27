package sit.int261.exceptiondemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sit.int261.exceptiondemo.controllers.RentalController;
import sit.int261.exceptiondemo.dtos.RentalDto;
import sit.int261.exceptiondemo.entities.Rental;
import sit.int261.exceptiondemo.exceptions.GlobalExceptionHandler;
import sit.int261.exceptiondemo.exceptions.GlobalExceptionHandler.*;
import sit.int261.exceptiondemo.services.RentalService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RentalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RentalService rentalService;

    @InjectMocks
    private RentalController rentalController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rentalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /rentals with validation error returns exact professor format")
    void testValidationErrorResponse() throws Exception {
        ValidationException ve = new ValidationException("Request validation failed");
        ve.addViolation("customerId", "must be greater than 0");
        when(rentalService.createRental(any(RentalDto.class))).thenThrow(ve);

        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": 0, \"inventoryId\": 1, \"staffId\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.violations[0].field").value("customerId"))
                .andExpect(jsonPath("$.violations[0].message").value("must be greater than 0"));
    }

    @Test
    @DisplayName("POST /rentals when Customer not found returns 404 with CUSTOMER_NOT_FOUND")
    void testCustomerNotFoundResponse() throws Exception {
        when(rentalService.createRental(any(RentalDto.class)))
                .thenThrow(new CustomerNotFoundException("Customer ID 999 not found"));

        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": 999, \"inventoryId\": 1, \"staffId\": 1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer ID 999 not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /rentals when Inventory unavailable returns 409 with INVENTORY_UNAVAILABLE")
    void testInventoryUnavailableResponse() throws Exception {
        when(rentalService.createRental(any(RentalDto.class)))
                .thenThrow(new InventoryUnavailableException("Inventory ID 10 is already rented"));

        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": 1, \"inventoryId\": 10, \"staffId\": 1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVENTORY_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Inventory ID 10 is already rented"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /rentals with malformed JSON body returns 400 with MALFORMED_JSON_REQUEST")
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON_REQUEST"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /rentals success returns 201 Created")
    void testCreateSuccess() throws Exception {
        when(rentalService.createRental(any(RentalDto.class))).thenReturn(new Rental());

        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": 1, \"inventoryId\": 1, \"staffId\": 1}"))
                .andExpect(status().isCreated());
    }
}
