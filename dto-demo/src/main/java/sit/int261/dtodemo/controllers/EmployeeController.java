package sit.int261.dtodemo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sit.int261.dtodemo.dtos.EmployeeSummaryDto;
import sit.int261.dtodemo.dtos.PageResponse;
import sit.int261.dtodemo.services.EmployeeService;

@RestController
@RequestMapping({"/api/v1/employees", "/employees"})
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<PageResponse<EmployeeSummaryDto>> getEmployees(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<EmployeeSummaryDto> response = employeeService.getEmployees(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(response);
    }
}
