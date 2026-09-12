package com.javarush.model.repository;

import com.javarush.model.entity.User;
import com.javarush.model.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByUserNameAndIdNot(String userName, Long id);

    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByUserName(String userName);
    Optional<User> findUserById(Long id);

    List<User> findAllUsersByRole(Role role);
}
