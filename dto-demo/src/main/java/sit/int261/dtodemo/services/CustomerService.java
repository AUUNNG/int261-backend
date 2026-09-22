package sit.int261.dtodemo.services;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int261.dtodemo.dtos.CustomerSalesProfileDto;
import sit.int261.dtodemo.dtos.PageResponse;
import sit.int261.dtodemo.entities.Customer;
import sit.int261.dtodemo.entities.Employee;
import sit.int261.dtodemo.entities.Office;
import sit.int261.dtodemo.repositories.CustomerRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public PageResponse<CustomerSalesProfileDto> getCustomers(Integer page, Integer size, String sortBy, String sortDir, String keyword, String country) {
        int pageNumber = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size <= 0) ? 10 : size;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort;

        if ("customerId".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(direction, "id");
        } else if ("creditLimit".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(direction, "creditLimit");
        } else {
            // Default sort: customerName asc
            sort = Sort.by(direction, "customerName");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Customer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Customer, Employee> salesRep = root.join("salesRepEmployee", JoinType.LEFT);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("customerName")), pattern);
                Predicate repFirstMatch = cb.like(cb.lower(salesRep.get("firstName")), pattern);
                Predicate repLastMatch = cb.like(cb.lower(salesRep.get("lastName")), pattern);
                predicates.add(cb.or(nameMatch, repFirstMatch, repLastMatch));
            }

            if (country != null && !country.trim().isEmpty()) {
                Join<Employee, Office> office = salesRep.join("office", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(office.get("country")), country.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Customer> customerPage = customerRepository.findAll(spec, pageable);

        List<CustomerSalesProfileDto> content = customerPage.getContent().stream()
                .map(this::toSalesProfileDto)
                .toList();

        return PageResponse.from(customerPage, content);
    }

    public CustomerSalesProfileDto toSalesProfileDto(Customer cust) {
        Employee rep = cust.getSalesRepEmployee();

        String repName = "Unassigned";
        String repEmail = null;
        String officeCity = null;
        String officeCountry = null;

        if (rep != null) {
            String fullName = "";
            if (rep.getFirstName() != null) fullName += rep.getFirstName();
            if (rep.getLastName() != null) {
                if (!fullName.isEmpty()) fullName += " ";
                fullName += rep.getLastName();
            }
            repName = fullName.trim();
            repEmail = rep.getEmail();

            Office office = rep.getOffice();
            if (office != null) {
                officeCity = office.getCity();
                officeCountry = office.getCountry();
            }
        }

        return CustomerSalesProfileDto.builder()
                .customerId(cust.getId())
                .customerName(cust.getCustomerName())
                .creditLimit(cust.getCreditLimit())
                .salesRepName(repName)
                .salesRepEmail(repEmail)
                .officeCity(officeCity)
                .officeCountry(officeCountry)
                .build();
    }
}
