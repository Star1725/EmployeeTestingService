package com.myservice.employeetestingservice.domain;

import jakarta.persistence.*;
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
public class UserStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userStorageName;
    private String storageDescription;
    private String logFile;
    private boolean isParentStorage;
    private boolean isChildStorage;
    private LocalDateTime dateCreated;
    private LocalDateTime dateChanged;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("userStorageName ASC")
    @ToString.Exclude
    private Set<UserStorage> childUserStorages;
    //------------------------------------------------------------------------------------------------------------------
//    public Set<User> getStorageUsers() {
//        storageUsers.addAll(getAllUsers());
//        return storageUsers;
//    }
//
//    private Set<User> getAllUsers() {
//        Set<User> storageUsers = new HashSet<>();
//        for (UserStorage childUserStorage : childUserStorages) {
//            storageUsers.addAll(childUserStorage.getAllUsers());
//        }
//        return storageUsers;
//    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("username ASC")
    @ToString.Exclude
    private Set<User> storageUsers;
    //------------------------------------------------------------------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentUserStorage_id")
    @ToString.Exclude
    private UserStorage parentUserStorage;

    @OneToOne
    @JoinColumn(name = "administrator_id")
    @ToString.Exclude
    private User administrator;

    @OneToOne
    @JoinColumn(name = "created_user_id")
    @ToString.Exclude
    private User createdUser;

    @OneToOne
    @JoinColumn(name = "changed_user_id")
    @ToString.Exclude
    private User changedUser;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserStorage that = (UserStorage) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public static Set<UserStorage> getAllNestedChildUserStorages(UserStorage rootStorage) {
        Set<UserStorage> result = new HashSet<>();
        collectChildStorages(rootStorage, result);
        return result;
    }

    private static void collectChildStorages(UserStorage storage, Set<UserStorage> result) {
        if (storage == null || storage.getChildUserStorages() == null) {
            return;
        }
        for (UserStorage child : storage.getChildUserStorages()) {
            if (result.add(child)) { // Проверяем, добавлен ли элемент, чтобы избежать зацикливания
                collectChildStorages(child, result);
            }
        }
    }
}
