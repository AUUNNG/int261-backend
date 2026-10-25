package sit.int261.int261sampleexam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private SimpleBrandDtoResponse brand;
    private BigDecimal price;
    private Integer stock;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
    private String description;
    private Boolean active;
}
