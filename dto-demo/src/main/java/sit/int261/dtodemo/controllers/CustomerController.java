package sit.int261.dtodemo.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.dtodemo.dtos.PageResponse;
import sit.int261.dtodemo.dtos.SimpleCustomerResponse;
import sit.int261.dtodemo.entities.Customer;
import sit.int261.dtodemo.repositories.CustomerRepository;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerRepository customerRepo;
    @Autowired
    private ModelMapper modelMapper;

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

    @GetMapping
    public ResponseEntity<List<SimpleCustomerResponse>> getCustomers() {
        List<Customer> customers = customerRepo.findAll();
        List<SimpleCustomerResponse> simpleCustomers = customers.stream()
        .map(e -> modelMapper
                .map(e, SimpleCustomerResponse.class)).toList();
        return ResponseEntity.ok(simpleCustomers);
    }

    @GetMapping("/page")
    public Object getCustomerPage(Pageable pageable) {
        Page<Customer> customerPage = customerRepo.findAll(pageable);
        List<SimpleCustomerResponse> content =  customerPage.getContent().stream()
                .map(e -> modelMapper.map(e, SimpleCustomerResponse.class)).toList();
        PageResponse<SimpleCustomerResponse> customer = modelMapper.map(customerPage, PageResponse.class);
        customer.setContent(content);
        return customer;
    }
}
