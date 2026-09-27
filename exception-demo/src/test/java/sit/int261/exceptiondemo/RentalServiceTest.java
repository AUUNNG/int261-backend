package sit.int261.exceptiondemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sit.int261.exceptiondemo.dtos.RentalDto;
import sit.int261.exceptiondemo.entities.Customer;
import sit.int261.exceptiondemo.entities.Inventory;
import sit.int261.exceptiondemo.entities.Rental;
import sit.int261.exceptiondemo.entities.Staff;
import sit.int261.exceptiondemo.exceptions.GlobalExceptionHandler.*;
import sit.int261.exceptiondemo.repositories.CustomerRepository;
import sit.int261.exceptiondemo.repositories.InventoryRepository;
import sit.int261.exceptiondemo.repositories.RentalRepository;
import sit.int261.exceptiondemo.repositories.StaffRepository;
import sit.int261.exceptiondemo.services.RentalService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private RentalService rentalService;

    private RentalDto validDto;

    @BeforeEach
    void setUp() {
        validDto = RentalDto.builder()
                .customerId(1)
                .inventoryId(2)
                .staffId(3)
                .build();
    }

    @Test
    @DisplayName("Should throw ValidationException when fields are missing or non-positive")
    void testValidationFailure() {
        RentalDto invalidDto = new RentalDto(null, -1, 0);

        ValidationException ex = assertThrows(ValidationException.class, () -> rentalService.createRental(invalidDto));

        assertEquals(3, ex.getViolations().size());
        assertTrue(ex.getViolations().stream().anyMatch(v -> v.getField().equals("customerId")));
        assertTrue(ex.getViolations().stream().anyMatch(v -> v.getField().equals("inventoryId")));
        assertTrue(ex.getViolations().stream().anyMatch(v -> v.getField().equals("staffId")));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when Customer is not found")
    void testCustomerNotFound() {
        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class, () -> rentalService.createRental(validDto));

        assertEquals("Customer ID 1 not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException when Inventory is not found")
    void testInventoryNotFound() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(new Customer()));
        when(inventoryRepository.findById(2)).thenReturn(Optional.empty());

        InventoryNotFoundException ex = assertThrows(InventoryNotFoundException.class, () -> rentalService.createRental(validDto));

        assertEquals("Inventory ID 2 not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw InventoryUnavailableException when Inventory is already rented")
    void testInventoryAlreadyRented() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(new Customer()));
        when(inventoryRepository.findById(2)).thenReturn(Optional.of(new Inventory()));
        when(rentalRepository.existsByInventoryIdAndReturnDateIsNull(2)).thenReturn(true);

        InventoryUnavailableException ex = assertThrows(InventoryUnavailableException.class, () -> rentalService.createRental(validDto));

        assertEquals("Inventory ID 2 is already rented", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw StaffNotFoundException when Staff is not found")
    void testStaffNotFound() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(new Customer()));
        when(inventoryRepository.findById(2)).thenReturn(Optional.of(new Inventory()));
        when(rentalRepository.existsByInventoryIdAndReturnDateIsNull(2)).thenReturn(false);
        when(staffRepository.findById((short) 3)).thenReturn(Optional.empty());

        StaffNotFoundException ex = assertThrows(StaffNotFoundException.class, () -> rentalService.createRental(validDto));

        assertEquals("Staff ID 3 not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should create Rental successfully when all validations and entities are valid")
    void testCreateRentalSuccess() {
        Customer customer = new Customer();
        Inventory inventory = new Inventory();
        Staff staff = new Staff();
        Rental expectedRental = new Rental();

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(inventoryRepository.findById(2)).thenReturn(Optional.of(inventory));
        when(rentalRepository.existsByInventoryIdAndReturnDateIsNull(2)).thenReturn(false);
        when(staffRepository.findById((short) 3)).thenReturn(Optional.of(staff));
        when(rentalRepository.save(any(Rental.class))).thenReturn(expectedRental);

        Rental result = rentalService.createRental(validDto);

        assertNotNull(result);
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }
}
