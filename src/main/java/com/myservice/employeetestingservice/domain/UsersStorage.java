package com.myservice.employeetestingservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ToString
@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class UsersStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String usersStorageName;
    private String storageDescription;
    private String logFile;
    private boolean isParentStorage;
    private boolean isChildrenStorage;
    private LocalDateTime dateCreated;
    private LocalDateTime dateChanged;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("usersStorageName ASC")
    @ToString.Exclude
    private Set<UsersStorage> childrenUsersStorages = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("username ASC")
    @ToString.Exclude
    private Set<User> storageUsers;

    @OneToOne
    @JoinColumn(name = "administrator_id")
    private User administrator;

    @OneToOne
    @JoinColumn(name = "created_user_id")
    private User createdUser;

    @OneToOne
    @JoinColumn(name = "changed_user_id")
    private User changedUser;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UsersStorage that = (UsersStorage) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
