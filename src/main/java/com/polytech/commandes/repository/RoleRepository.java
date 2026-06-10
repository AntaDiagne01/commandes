package com.polytech.commandes.repository;

import com.polytech.commandes.entity.Role;
import com.polytech.commandes.entity.Role.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleEnum name);
}