package sit.int261.int261sampleexam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@ToString
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "ram_gb", nullable = false)
    private Integer ramGb;

    @Column(name = "storage_gb", nullable = false)
    private Integer storageGb;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active;
}
