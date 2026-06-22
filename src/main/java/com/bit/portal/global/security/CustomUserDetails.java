package com.bit.portal.global.security;

import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.enums.EmployeeStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Employee employee;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(employee.getRole().name()));
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @Override
    public String getUsername() {
        return employee.getEmail();
    }

    /** ACTIVE 상태인 경우만 true — 퇴사자는 false → 접근 즉시 차단 */
    @Override
    public boolean isEnabled() {
        return employee.getStatus() == EmployeeStatus.ACTIVE;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    public int getTokenVersion() {
        return employee.getTokenVersion();
    }

    public Long getEmployeeId() {
        return employee.getId();
    }
}
