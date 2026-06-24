package com.bit.portal.domain.employee.repository;

import com.bit.portal.domain.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query(
        "SELECT MAX(e.employeeId) FROM Employee e WHERE e.employeeId LIKE CONCAT(:prefix, '%')"
    )
    java.util.Optional<String> findMaxEmployeeIdByPrefix(@org.springframework.data.repository.query.Param("prefix") String prefix);
}
