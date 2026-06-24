package com.bit.portal.domain.employee.service;

import com.bit.portal.domain.employee.dto.*;
import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.enums.EmployeeStatus;
import com.bit.portal.domain.employee.repository.EmployeeRepository;
import com.bit.portal.global.error.code.ErrorCode;
import com.bit.portal.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Admin: 직원 생성 ──────────────────────────────────────────────────────

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String employeeId = generateEmployeeId();
        Employee employee = Employee.create(
                employeeId,
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getDepartment(),
                request.getPosition(),
                request.getDateOfBirth(),
                request.getHireDate(),
                request.getRole()
        );

        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // ── Admin: 직원 목록 (페이지네이션 + 정렬) ────────────────────────────────

    public Page<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findAll(pageable).map(EmployeeResponse::from);
    }

    // ── Admin: 직원 상세 ──────────────────────────────────────────────────────

    public EmployeeResponse getEmployee(Long id) {
        return EmployeeResponse.from(findById(id));
    }

    // ── Admin: 직원 정보 수정 (부서·직책·권한) ────────────────────────────────

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeAdminUpdateRequest request) {
        Employee employee = findById(id);
        employee.updateByAdmin(request.getDepartment(), request.getPosition(), request.getRole());
        return EmployeeResponse.from(employee);
    }

    // ── Admin: 상태 변경 (퇴사 처리만 허용) ──────────────────────────────────

    @Transactional
    public EmployeeResponse updateStatus(Long id, EmployeeStatusUpdateRequest request) {
        Employee employee = findById(id);

        if (employee.getStatus() == EmployeeStatus.RESIGNED) {
            throw new BusinessException(ErrorCode.ALREADY_RESIGNED);
        }

        if (request.getStatus() == EmployeeStatus.RESIGNED) {
            employee.resign();
        }

        return EmployeeResponse.from(employee);
    }

    // ── 직원 본인: 내 정보 조회/수정 ──────────────────────────────────────────

    public EmployeeResponse getMyProfile(String email) {
        return EmployeeResponse.from(findByEmail(email));
    }

    @Transactional
    public EmployeeResponse updateMyProfile(String email, EmployeeUpdateRequest request) {
        Employee employee = findByEmail(email);
        employee.updateProfile(
                request.getPhone(),
                request.getAddress(),
                request.getEmergencyContactName(),
                request.getEmergencyContactPhone(),
                request.getNote()
        );
        return EmployeeResponse.from(employee);
    }

    // ── 테스트 직원 30명 생성 (DataInitializer 전용, SEED_EMPLOYEES=true 시 실행) ──

    @Transactional
    public void createTestEmployees(String defaultPassword) {
        if (employeeRepository.existsByEmail("minjun.kim@bit.com")) {
            log.info("Test employees already seeded, skipping");
            return;
        }

        record Seed(String lastName, String firstName, String email,
                    String dept, String pos,
                    int by, int bm, int bd, int hy, int hm, int hd) {}

        var seeds = java.util.List.of(
            // 개발팀 7명
            new Seed("Kim",  "Minjun",    "minjun.kim@bit.com",    "개발팀", "사원", 1998, 3,15, 2023, 3, 2),
            new Seed("Lee",  "Sooyeon",   "sooyeon.lee@bit.com",   "개발팀", "대리", 1994, 7,22, 2020, 7, 1),
            new Seed("Park", "Junhyuk",   "junhyuk.park@bit.com",  "개발팀", "과장", 1990,11, 5, 2018, 4, 1),
            new Seed("Choi", "Jisoo",     "jisoo.choi@bit.com",    "개발팀", "차장", 1987, 2,18, 2015, 9, 1),
            new Seed("Jung", "Taewan",    "taewan.jung@bit.com",   "개발팀", "팀장", 1985, 6,30, 2013, 2, 1),
            new Seed("Kang", "Heejin",    "heejin.kang@bit.com",   "개발팀", "사원", 1999, 9,12, 2024, 1, 2),
            new Seed("Yoon", "Seunghoon", "seunghoon.yoon@bit.com","개발팀", "대리", 1995, 4,28, 2021, 5, 3),
            // 마케팅팀 5명
            new Seed("Jang", "Nayeon",    "nayeon.jang@bit.com",   "마케팅팀","팀장", 1986, 8,15, 2014, 3, 1),
            new Seed("Lim",  "Woojin",    "woojin.lim@bit.com",    "마케팅팀","과장", 1991,12, 3, 2019, 6, 1),
            new Seed("Han",  "Chaeyeon",  "chaeyeon.han@bit.com",  "마케팅팀","대리", 1996, 5,20, 2022, 1, 3),
            new Seed("Oh",   "Minseok",   "minseok.oh@bit.com",    "마케팅팀","사원", 2000, 1, 7, 2024, 3, 4),
            new Seed("Seo",  "Yeonhee",   "yeonhee.seo@bit.com",   "마케팅팀","사원", 1999,11,25, 2023, 9, 1),
            // 인사팀 5명
            new Seed("Shin", "Jiyoung",   "jiyoung.shin@bit.com",  "인사팀", "팀장", 1984, 4,10, 2012, 8, 1),
            new Seed("Kwon", "Sanghoon",  "sanghoon.kwon@bit.com", "인사팀", "차장", 1988, 9,16, 2016, 1, 4),
            new Seed("Hong", "Minji",     "minji.hong@bit.com",    "인사팀", "과장", 1992, 3,22, 2019,11, 1),
            new Seed("Kim",  "Eunji",     "eunji.kim@bit.com",     "인사팀", "대리", 1997, 6,14, 2022, 6, 1),
            new Seed("Lee",  "Junho",     "junho.lee@bit.com",     "인사팀", "사원", 2001, 2,28, 2024, 7, 1),
            // 재무팀 5명
            new Seed("Park", "Sunyoung",  "sunyoung.park@bit.com", "재무팀", "팀장", 1983,10, 5, 2011, 4, 1),
            new Seed("Choi", "Jaewon",    "jaewon.choi@bit.com",   "재무팀", "과장", 1989, 7,18, 2017, 8, 1),
            new Seed("Jung", "Haerin",    "haerin.jung@bit.com",   "재무팀", "대리", 1993, 1,30, 2020,10, 5),
            new Seed("Kang", "Donghyun",  "donghyun.kang@bit.com", "재무팀", "사원", 1998, 8,22, 2023, 6, 1),
            new Seed("Yoon", "Yejin",     "yejin.yoon@bit.com",    "재무팀", "사원", 2000, 4,15, 2024, 2, 1),
            // 영업팀 5명
            new Seed("Jang", "Hyunwoo",   "hyunwoo.jang@bit.com",  "영업팀", "팀장", 1985,12, 1, 2013, 7, 1),
            new Seed("Lim",  "Soojin",    "soojin.lim@bit.com",    "영업팀", "차장", 1989, 3, 8, 2016, 9, 1),
            new Seed("Han",  "Kyungjae",  "kyungjae.han@bit.com",  "영업팀", "과장", 1993, 9,25, 2020, 3, 2),
            new Seed("Oh",   "Jiwon",     "jiwon.oh@bit.com",      "영업팀", "대리", 1996,11,12, 2022, 9, 1),
            new Seed("Seo",  "Taehyun",   "taehyun.seo@bit.com",   "영업팀", "사원", 1999, 5, 3, 2023,12, 1),
            // 운영팀 3명
            new Seed("Shin", "Daeyoung",  "daeyoung.shin@bit.com", "운영팀", "팀장", 1986, 7,20, 2015, 5, 1),
            new Seed("Kwon", "Yuna",      "yuna.kwon@bit.com",     "운영팀", "대리", 1995, 2, 8, 2021,11, 1),
            new Seed("Hong", "Subin",     "subin.hong@bit.com",    "운영팀", "사원", 2000, 9,30, 2024, 5, 1)
        );

        String encoded = passwordEncoder.encode(defaultPassword);
        for (var s : seeds) {
            Employee emp = Employee.create(
                    generateEmployeeId(), s.email(), encoded,
                    s.firstName(), s.lastName(),
                    s.dept(), s.pos(),
                    LocalDate.of(s.by(), s.bm(), s.bd()),
                    LocalDate.of(s.hy(), s.hm(), s.hd()),
                    com.bit.portal.domain.employee.enums.Role.ROLE_USER
            );
            employeeRepository.save(emp);
        }
        log.info("Seeded {} test employees (pw: {})", seeds.size(), defaultPassword);
    }

    // ── 초기 Admin 계정 생성 (DataInitializer 전용) ───────────────────────────

    @Transactional
    public void createInitialAdmin(String email, String rawPassword) {
        if (employeeRepository.existsByEmail(email)) {
            return;
        }
        Employee admin = Employee.create(
                "EMP-" + LocalDate.now().getYear() + "-001",
                email,
                passwordEncoder.encode(rawPassword),
                "Admin", "System",
                "IT", "System Administrator",
                LocalDate.of(1990, 1, 1),
                LocalDate.now(),
                com.bit.portal.domain.employee.enums.Role.ROLE_ADMIN
        );
        employeeRepository.save(admin);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private String generateEmployeeId() {
        int year = LocalDate.now().getYear();
        String prefix = "EMP-" + year + "-";
        // MAX 방식: 삭제 후 재생성 시 중복 없음, count 방식 대비 안전
        int next = employeeRepository.findMaxEmployeeIdByPrefix(prefix)
                .map(maxId -> Integer.parseInt(maxId.substring(prefix.length())) + 1)
                .orElse(1);
        return String.format("EMP-%d-%03d", year, next);
    }
}
