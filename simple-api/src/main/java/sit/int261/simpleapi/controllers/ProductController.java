package sit.int261.simpleapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
class ProductController {
    @GetMapping
    public List<String> getProduct() {
        return List.of("Product 1", "Product 2", "Product 3", "Product 4", "Product 5");
    }

    @GetMapping("/{id}")
    public String getProductById(@PathVariable String id) {
        return "Brand - " + id;
    }
}
