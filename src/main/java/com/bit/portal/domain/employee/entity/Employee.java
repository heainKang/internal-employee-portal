package com.bit.portal.domain.employee.entity;

import com.bit.portal.domain.employee.enums.EmployeeStatus;
import com.bit.portal.domain.employee.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(length = 500)
    private String note;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String position;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** 퇴사/강제 로그아웃 시 증가 → 기존 JWT 즉시 무효화 */
    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Factory ──────────────────────────────────────────────────────────────

    public static Employee create(String employeeId, String email, String encodedPassword,
                                  String firstName, String lastName,
                                  String department, String position,
                                  LocalDate dateOfBirth, LocalDate hireDate, Role role) {
        Employee e = new Employee();
        e.employeeId = employeeId;
        e.email = email;
        e.password = encodedPassword;
        e.firstName = firstName;
        e.lastName = lastName;
        e.department = department;
        e.position = position;
        e.dateOfBirth = dateOfBirth;
        e.hireDate = hireDate;
        e.role = role;
        e.status = EmployeeStatus.ACTIVE;
        e.tokenVersion = 0;
        return e;
    }

    // ── Domain Methods ────────────────────────────────────────────────────────

    /** 퇴사 처리: 상태 변경 + tokenVersion 증가로 기존 토큰 즉시 무효화 */
    public void resign() {
        this.status = EmployeeStatus.RESIGNED;
        this.tokenVersion++;
    }

    /** 직원 본인 수정 가능 필드 — 이메일 변경 불가, 빈 문자열 전송 시 null로 초기화 */
    public void updateProfile(String phone, String address, String emergencyContactName,
                              String emergencyContactPhone, String note) {
        this.phone                = blankToNull(phone);
        this.address              = blankToNull(address);
        this.emergencyContactName = blankToNull(emergencyContactName);
        this.emergencyContactPhone = blankToNull(emergencyContactPhone);
        this.note                 = blankToNull(note);
    }

    private static String blankToNull(String v) {
        return (v != null && !v.isBlank()) ? v : null;
    }

    /** 관리자 수정 가능 필드 — 빈 문자열 전송 시 null로 초기화(삭제) */
    public void updateByAdmin(String department, String position, Role role) {
        this.department = (department != null && !department.isBlank()) ? department : null;
        this.position  = (position  != null && !position.isBlank())  ? position  : null;
        if (role != null) this.role = role;
    }
}
