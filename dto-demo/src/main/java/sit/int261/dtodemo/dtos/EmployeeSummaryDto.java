package sit.int261.dtodemo.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSummaryDto {
    private Integer id;
    private String fullName;
    private String email;
    private String phoneExtension;
    private String jobTitle;
}
