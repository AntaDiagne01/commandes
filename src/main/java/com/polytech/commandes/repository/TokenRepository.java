package com.polytech.commandes.repository;

import com.polytech.commandes.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    List<Token> findByUtilisateurIdAndRevokedFalse(Long utilisateurId);
}