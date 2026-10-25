package sit.int261.int261sampleexam.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import sit.int261.int261sampleexam.dtos.BrandResponse;
import sit.int261.int261sampleexam.services.BrandService;
import sit.int261.int261sampleexam.utils.RequestParamCheck;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/brands")
public class BrandsControllers {

    private final BrandService brandService;

    public BrandsControllers(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> findAll(WebRequest req) {
        RequestParamCheck.allowOnly(req);
        return ResponseEntity.ok(brandService.findAll());
    }
}
