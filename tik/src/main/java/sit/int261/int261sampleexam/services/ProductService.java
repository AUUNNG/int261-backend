package sit.int261.int261sampleexam.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int261.int261sampleexam.dtos.ProductRequest;
import sit.int261.int261sampleexam.entities.Brand;
import sit.int261.int261sampleexam.entities.Product;
import sit.int261.int261sampleexam.exceptions.BrandNotFoundException;
import sit.int261.int261sampleexam.exceptions.DuplicateSkuException;
import sit.int261.int261sampleexam.exceptions.ProductNotFoundException;
import sit.int261.int261sampleexam.repositories.BrandRepository;
import sit.int261.int261sampleexam.repositories.ProductRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final BrandRepository brandRepository;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper, BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.brandRepository = brandRepository;
    }

    public List<Product> findAll(Long brandId) {
        if (brandId == null) {
            return productRepository.findAllByOrderByIdAsc();
        }

        if (!brandRepository.existsById(brandId)) {
            throw new BrandNotFoundException("Brand not found for id " + brandId);
        }

        return productRepository.findByBrandIdOrderByIdAsc(brandId);
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found for id " + id)
        );
    }

    @Transactional
    public Product createProduct(ProductRequest productRequest) {
        Long brandId = productRequest.getBrandId().longValue();
        Brand brand = brandRepository.findById(brandId).orElseThrow(
                () -> new BrandNotFoundException("Brand not found for id " + brandId)
        );

        if (productRepository.existsBySku(productRequest.getSku())) {
            throw new DuplicateSkuException("Sku already Exits");
        }

        Product product = modelMapper.map(productRequest, Product.class);
        product.setId(null);
        product.setBrand(brand);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest req) {
        // ลำดับตรวจตามข้อ 6: product exists -> brand exists -> SKU uniqueness -> write
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found for id " + id)
        );

        Long brandId = req.getBrandId().longValue();
        Brand brand = brandRepository.findById(brandId).orElseThrow(
                () -> new BrandNotFoundException("Brand not found for id " + brandId)
        );

        if (productRepository.existsBySkuAndIdNot(req.getSku(), id)) {
            throw new DuplicateSkuException("Sku already Exits");
        }

        product.setSku(req.getSku());
        product.setName(req.getName());
        product.setBrand(brand);
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setRamGb(req.getRamGb());
        product.setStorageGb(req.getStorageGb());
        product.setColor(req.getColor());
        product.setDescription(req.getDescription());
        product.setActive(req.getActive());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found for id " + id)
        );

        productRepository.delete(product);
    }
}
