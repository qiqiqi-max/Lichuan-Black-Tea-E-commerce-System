package com.lichuan.tea.repository;

import com.lichuan.tea.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("SELECT u FROM User u " +
            "WHERE (:username IS NULL OR u.username LIKE %:username%) " +
            "AND (:nickname IS NULL OR u.nickname LIKE %:nickname%) " +
            "AND (:phone IS NULL OR u.phone LIKE %:phone%) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "ORDER BY u.id DESC")
    Page<User> findManagePage(@Param("username") String username,
                              @Param("nickname") String nickname,
                              @Param("phone") String phone,
                              @Param("role") String role,
                              Pageable pageable);
}
