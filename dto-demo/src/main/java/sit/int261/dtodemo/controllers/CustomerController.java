package sit.int261.dtodemo.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.dtodemo.dtos.CustomerSalesProfileDto;
import sit.int261.dtodemo.dtos.PageResponse;
import sit.int261.dtodemo.dtos.SimpleCustomerResponse;
import sit.int261.dtodemo.entities.Customer;
import sit.int261.dtodemo.repositories.CustomerRepository;
import sit.int261.dtodemo.services.CustomerService;

@RestController
@RequestMapping({"/api/v1/customers", "/customers"})
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<PageResponse<CustomerSalesProfileDto>> getCustomers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "customerName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String country
    ) {
        PageResponse<CustomerSalesProfileDto> response = customerService.getCustomers(page, size, sortBy, sortDir, keyword, country);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleCustomerResponse> getCustomer(@PathVariable Integer id) {
        Customer customer = customerRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer with id " + id + " not found"
                )
        );
        SimpleCustomerResponse simpleCustomer = modelMapper.map(customer, SimpleCustomerResponse.class);
        return ResponseEntity.ok(simpleCustomer);
    }
}
