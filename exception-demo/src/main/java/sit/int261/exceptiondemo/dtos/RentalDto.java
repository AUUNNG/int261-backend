package sit.int261.exceptiondemo.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalDto {
    private Integer customerId;
    private Integer inventoryId;
    private Integer staffId;
}
