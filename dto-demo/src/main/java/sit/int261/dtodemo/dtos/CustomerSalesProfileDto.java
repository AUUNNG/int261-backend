package sit.int261.dtodemo.dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSalesProfileDto {
    private Integer customerId;
    private String customerName;
    private BigDecimal creditLimit;
    private String salesRepName;
    private String salesRepEmail;
    private String officeCity;
    private String officeCountry;
}
