package com.myservice.employeetestingservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "usr")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;
    private String password;
    private LocalDateTime dateCreated;
    private LocalDateTime dateChanged;
    private String logFile;
    private String specialNotes;
    private boolean active;

    @OneToOne
    @JoinColumn(name = "created_user_id")
    private User createdUser;

    @OneToOne
    @JoinColumn(name = "changed_user_id")
    private User changedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    @ToString.Exclude
    private User administrator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usersStorage_id")
    @ToString.Exclude
    private UsersStorage usersStorage;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @OrderBy
    private List<Role> roles;

    public boolean isAdmin(){
        return roles.contains(Role.ADMIN);
    }
    public boolean isUser(){
        return roles.contains(Role.USER);
    }
    public boolean isMainAdmin(){
        return roles.contains(Role.MAIN_ADMIN);
    }

    @ElementCollection(targetClass = AccessLevel.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_access", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @OrderBy
    private List<AccessLevel> accessLevels;

    public boolean isAccessToTS(){
        return accessLevels.contains(AccessLevel.LEVEL_1);
    }
    public boolean isAccessToS(){
        return accessLevels.contains(AccessLevel.LEVEL_2);
    }
    public boolean isAccessToFOU(){
        return accessLevels.contains(AccessLevel.LEVEL_3);
    }

    @ElementCollection(targetClass = SpecAccess.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_spec_access", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @OrderBy
    private List<SpecAccess> specAccesses;

    public boolean isAccessToSD(){
        return specAccesses.contains(SpecAccess.SPEC_ACCESS_1);
    }
    public boolean isAccessToDSP(){
        return specAccesses.contains(SpecAccess.SPEC_ACCESS_2);
    }
    public boolean isAccessToRukSos(){
        return specAccesses.contains(SpecAccess.SPEC_ACCESS_3);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
