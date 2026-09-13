package com.javarush.model.repository;

import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.deleted = false")
    Set<User> findAllActive();

    @Query("select u from User u where u.deleted = false")
    Page<User> findAllActive(Pageable pageable);

    @Query("select u from User u where u.deleted = false and u.id = :id")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("select u from User u where u.deleted = false and u.username = :username")
    Optional<User> findActiveByUsername(@Param("username") String username);

    @Query("select u from User u where u.deleted = false and u.email = :email")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("select u from User u where u.deleted = false and u.role = :role")
    List<User> findActiveByRole(@Param("role") Role role);

    @Query("select u from User u where u.deleted = false and u.id in :ids")
    Set<User> findAllActiveByIds(@Param("ids") Set<Long> ids);

    @Query("select count(u) > 0 from User u where u.deleted = false and u.email = :email")
    boolean existsActiveByEmail(@Param("email") String email);

    @Query("select count(u) > 0 from User u where u.deleted = false and u.username = :username")
    boolean existsActiveByUsername(@Param("username") String username);

    @Query("select count(u) > 0 from User u where u.deleted = false and u.email = :email and u.id <> :id")
    boolean existsActiveByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Query("select count(u) > 0 from User u where u.deleted = false and u.username = :username and u.id <> :id")
    boolean existsActiveByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);

    @Query("select u from User u where u.deleted = true")
    Set<User> findAllDeleted();

    @Query("select u from User u where u.deleted = true and u.id = :id")
    Optional<User> findDeletedById(@Param("id") Long id);
}