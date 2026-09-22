package sit.int261.dtodemo.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBasicDto {
    private Integer customerId;
    private String customerName;
    private String phone;
}
