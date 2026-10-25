package sit.int261.int261sampleexam.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import sit.int261.int261sampleexam.dtos.ProductRequest;
import sit.int261.int261sampleexam.dtos.ProductResponse;
import sit.int261.int261sampleexam.entities.Product;
import sit.int261.int261sampleexam.exceptions.InvalidRequestException;
import sit.int261.int261sampleexam.services.ProductService;
import sit.int261.int261sampleexam.utils.RequestParamCheck;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ModelMapper modelMapper;
    private final ProductService productService;

    public ProductController(ModelMapper modelMapper, ProductService productService) {
        this.modelMapper = modelMapper;
        this.productService = productService;
    }

    private Long parsePathId(String idStr) {
        // Path id ต้องเป็น positive integer แบบ decimal digits เท่านั้น ไม่รับ +1, 1.0, 0, -1, abc หรือ overflow: 400 INVALID_REQUEST
        if (idStr == null || !idStr.matches("^[0-9]+$")) {
            throw new InvalidRequestException("Invalid product id: must be positive decimal digits only");
        }
        try {
            long id = Long.parseLong(idStr);
            if (id < 1) {
                throw new InvalidRequestException("Invalid product id: must be positive integer");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid product id: number overflow");
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(
            @RequestParam(required = false) String brandId, WebRequest req
    ) {
        RequestParamCheck.allowOnly(req, "brandId");
        Long brand = null;
        if (brandId != null) {
            // ค่าว่าง, ไม่ใช่ integer, <=0, เกิน BIGINT = 400 INVALID_REQUEST
            if (!brandId.matches("^[0-9]+$")) {
                throw new InvalidRequestException("Invalid brandId: must be positive integer decimal digits");
            }
            try {
                brand = Long.parseLong(brandId);
            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Invalid brandId: number overflow");
            }
            if (brand < 1) {
                throw new InvalidRequestException("Invalid brandId: must be positive");
            }
        }

        List<Product> products = productService.findAll(brand);
        List<ProductResponse> productResponseList = products.stream()
                .map(e -> modelMapper
                        .map(e, ProductResponse.class)).toList();
        return ResponseEntity.ok(productResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id, WebRequest req) {
        RequestParamCheck.allowOnly(req);
        Long validId = parsePathId(id);
        Product product = productService.findById(validId);
        ProductResponse productResponse = modelMapper.map(product, ProductResponse.class);
        return ResponseEntity.ok(productResponse);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest productRequest,
            WebRequest req
    ) {
        RequestParamCheck.allowOnly(req);
        ProductResponse productResponse = modelMapper.map(
                productService.createProduct(productRequest),
                ProductResponse.class
        );
        URI location = URI.create("/api/v1/products/" + productResponse.getId());
        return ResponseEntity.created(location).body(productResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id, WebRequest req) {
        RequestParamCheck.allowOnly(req);
        Long validId = parsePathId(id);
        productService.deleteProduct(validId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest productRequest,
            WebRequest req
    ) {
        RequestParamCheck.allowOnly(req);
        Long validId = parsePathId(id);
        Product updated = productService.updateProduct(validId, productRequest);
        return ResponseEntity.ok(modelMapper.map(updated, ProductResponse.class));
    }
}
