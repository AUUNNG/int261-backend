package sit.int261.simpleapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
class CustomerController {
    @GetMapping
    public Object getCustomers() {
        return List.of("Customer1", "Customer2", "Customer3");
    }

    @GetMapping("/{id}")
    public Object getCustomer(@PathVariable String id) {
        return List.of("Customer" + id);
    }
}
