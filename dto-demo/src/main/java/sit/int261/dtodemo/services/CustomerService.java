package sit.int261.dtodemo.services;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.dtodemo.dtos.NewCustomerDto;
import sit.int261.dtodemo.entities.Customer;
import sit.int261.dtodemo.entities.Employee;
import sit.int261.dtodemo.repositories.CustomerRepository;
import sit.int261.dtodemo.repositories.EmployeeRopository;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EmployeeRopository employeeRopository;
    @Autowired
    private ModelMapper modelMapper;

    public Customer getCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer does not exist for id " + id
                )
        );

        return customer;
    }

    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    public Page<Customer> getCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Customer createCustomer(@Valid @RequestBody NewCustomerDto newCustomerDto) {
        Integer empId = newCustomerDto.getId();
        Employee employee = employeeRopository.findById(empId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee does not exist for id " + empId)
        );
        Customer newCustomer = modelMapper.map(newCustomerDto, Customer.class);
        newCustomer.setSalesRepEmployee(employee);
        return customerRepository.save(newCustomer);
    }
}