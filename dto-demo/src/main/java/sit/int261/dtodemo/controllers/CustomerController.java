package sit.int261.dtodemo.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.dtodemo.dtos.NewCustomerDto;
import sit.int261.dtodemo.dtos.PageResponse;
import sit.int261.dtodemo.dtos.SimpleCustomerResponse;
import sit.int261.dtodemo.entities.Customer;
import sit.int261.dtodemo.services.CustomerService;
import sit.int261.dtodemo.exception.ErrorResponse;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<Object> createCustomer(@Valid @RequestBody NewCustomerDto newCustomerDto){
        return ResponseEntity.ok(
                modelMapper.map(customerService.createCustomer((newCustomerDto)), SimpleCustomerResponse.class)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleCustomerResponse> getCustomer(
            @PathVariable Integer id) {

        Customer customer = customerService.getCustomerById(id);

        SimpleCustomerResponse simpleCustomer = modelMapper.map(
                customer,
                SimpleCustomerResponse.class
        );

        return ResponseEntity.ok(simpleCustomer);
    }


    @GetMapping
    public ResponseEntity<List<SimpleCustomerResponse>> getCustomers() {

        List<Customer> customers = customerService.getCustomers();

        List<SimpleCustomerResponse> simpleCustomers = customers.stream()
                .map(e -> modelMapper.map(
                        e,
                        SimpleCustomerResponse.class
                ))
                .toList();

        return ResponseEntity.ok(simpleCustomers);
    }

    @GetMapping("/page")
    public PageResponse<SimpleCustomerResponse> getCustomersPage(
            Pageable pageable) {

        Page<Customer> customerPage =
                customerService.getCustomers(pageable);

        List<SimpleCustomerResponse> contents =
                customerPage.getContent().stream()
                        .map(e -> modelMapper.map(
                                e,
                                SimpleCustomerResponse.class
                        ))
                        .toList();

        PageResponse<SimpleCustomerResponse> customers =
                modelMapper.map(
                        customerPage,
                        PageResponse.class
                );

        customers.setContent(contents);

        return customers;
    }

//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                e.getStatusCode().value(), e.getMessage(),"/customers/{id}"
//        );
//        errorResponse.setStackTrace("X1295C");
//        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
//    }
}