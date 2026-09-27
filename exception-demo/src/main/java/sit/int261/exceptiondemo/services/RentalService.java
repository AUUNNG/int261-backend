package sit.int261.exceptiondemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

import java.time.Instant;

@Service
public class RentalService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private RentalRepository rentalRepository;

    public Rental createRental(RentalDto rentalDto) {
        ValidationException validationException = new ValidationException("Request validation failed");

        if (rentalDto.getCustomerId() == null) {
            validationException.addViolation("customerId", "must be present");
        } else if (rentalDto.getCustomerId() <= 0) {
            validationException.addViolation("customerId", "must be greater than 0");
        }

        if (rentalDto.getInventoryId() == null) {
            validationException.addViolation("inventoryId", "must be present");
        } else if (rentalDto.getInventoryId() <= 0) {
            validationException.addViolation("inventoryId", "must be greater than 0");
        }

        if (rentalDto.getStaffId() == null) {
            validationException.addViolation("staffId", "must be present");
        } else if (rentalDto.getStaffId() <= 0) {
            validationException.addViolation("staffId", "must be greater than 0");
        }

        if (validationException.hasViolations()) {
            throw validationException;
        }

        Customer customer = customerRepository.findById(rentalDto.getCustomerId()).orElseThrow(
                () -> new CustomerNotFoundException("Customer ID " + rentalDto.getCustomerId() + " not found")
        );

        Inventory inventory = inventoryRepository.findById(rentalDto.getInventoryId()).orElseThrow(
                () -> new InventoryNotFoundException("Inventory ID " + rentalDto.getInventoryId() + " not found")
        );

        if (rentalRepository.existsByInventoryIdAndReturnDateIsNull(rentalDto.getInventoryId())) {
            throw new InventoryUnavailableException("Inventory ID " + rentalDto.getInventoryId() + " is already rented");
        }

        Staff staff = staffRepository.findById(rentalDto.getStaffId().shortValue()).orElseThrow(
                () -> new StaffNotFoundException("Staff ID " + rentalDto.getStaffId() + " not found")
        );

        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setInventory(inventory);
        rental.setStaff(staff);
        rental.setRentalDate(Instant.now());
        rental.setLastUpdate(Instant.now());

        return rentalRepository.save(rental);
    }
}
