package sit.int261.dtodemo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import sit.int261.dtodemo.entities.Employee;

@Getter
@Setter
public class SimpleCustomerResponse {
    private String customerName;
    private String phone;
    private String city;
    private String country;
//    @JsonIgnore
    private SimpleEmployeeResponse salesRepEmployee;
//    private String salesPerson;

//    public String getSalePerson () {
//        return salesRepEmployee==null ? "-": salesRepEmployee.getFirstName() + " " + salesRepEmployee.getLastName();
//    }

}

