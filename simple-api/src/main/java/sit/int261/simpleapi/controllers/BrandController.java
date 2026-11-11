package sit.int261.simpleapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/brands")
class BrandController {
    @GetMapping
    public List<String> getBrand() {
        return List.of("Brand 1", "Brand 2", "Brand 3", "Brand 4", "Brand 5");
    }

    @GetMapping("/{id}")
    public String getBrandById(@PathVariable String id) {
        return "Brand - " + id;
    }
}
