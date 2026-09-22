package sit.int261.dtodemo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDto {
    private Integer orderId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    private String status;
    private String customerName;
    private Integer totalItems;
    private BigDecimal totalAmount;
}
