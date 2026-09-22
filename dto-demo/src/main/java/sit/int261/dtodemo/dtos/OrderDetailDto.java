package sit.int261.dtodemo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDto {
    private Integer orderId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    private String status;
    private CustomerBasicDto customer;

    @Builder.Default
    private List<OrderItemDto> items = new ArrayList<>();

    private BigDecimal totalAmount;
}
