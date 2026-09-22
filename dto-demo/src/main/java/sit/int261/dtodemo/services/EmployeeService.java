package sit.int261.dtodemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int261.dtodemo.dtos.EmployeeSummaryDto;
import sit.int261.dtodemo.dtos.PageResponse;
import sit.int261.dtodemo.entities.Employee;
import sit.int261.dtodemo.repositories.EmployeeRepository;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public PageResponse<EmployeeSummaryDto> getEmployees(Integer page, Integer size, String sortBy, String sortDir, String keyword) {
        int pageNumber = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size <= 0) ? 10 : size;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort;

        if ("fullName".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(direction, "firstName", "lastName");
        } else if ("jobTitle".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(direction, "jobTitle");
        } else {
            // Default sort by id asc
            sort = Sort.by(direction, "id");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Employee> spec = (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };

        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);

        List<EmployeeSummaryDto> content = employeePage.getContent().stream()
                .map(this::toSummaryDto)
                .toList();

        return PageResponse.from(employeePage, content);
    }

    private EmployeeSummaryDto toSummaryDto(Employee emp) {
        String fullName = "";
        if (emp.getFirstName() != null) fullName += emp.getFirstName();
        if (emp.getLastName() != null) {
            if (!fullName.isEmpty()) fullName += " ";
            fullName += emp.getLastName();
        }

        return EmployeeSummaryDto.builder()
                .id(emp.getId())
                .fullName(fullName.trim())
                .email(emp.getEmail())
                .phoneExtension(emp.getExtension())
                .jobTitle(emp.getJobTitle())
                .build();
    }
}
