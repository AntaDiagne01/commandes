package com.polytech.commandes.service;

import com.polytech.commandes.dto.AuthResponse;
import com.polytech.commandes.dto.LoginRequest;
import com.polytech.commandes.dto.RegisterRequest;
import com.polytech.commandes.entity.Client;
import com.polytech.commandes.entity.Role;
import com.polytech.commandes.entity.Role.RoleEnum;
import com.polytech.commandes.entity.Token;
import com.polytech.commandes.entity.Utilisateur;
import com.polytech.commandes.repository.ClientRepository;
import com.polytech.commandes.repository.RoleRepository;
import com.polytech.commandes.repository.TokenRepository;
import com.polytech.commandes.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UtilisateurDetailsService utilisateurDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier que le username n'existe pas déjà
        if (utilisateurRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ce username est déjà pris : " + request.getUsername());
        }

        // Récupérer ou créer le rôle USER
        Role roleUser = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(RoleEnum.ROLE_USER).build()
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(roleUser);

        // Créer l'utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        // Créer automatiquement un Client associé
        Client client = Client.builder()
                .nom(request.getNom() != null ? request.getNom() : request.getUsername())
                .email(request.getEmail())
                .utilisateur(utilisateur)
                .build();
        clientRepository.save(client);

        // Générer le token JWT
        UserDetails userDetails = utilisateurDetailsService.loadUserByUsername(utilisateur.getUsername());
        String jwt = jwtService.generateToken(userDetails);

        // Sauvegarder le token
        saveToken(utilisateur, jwt);

        return AuthResponse.builder()
                .accessToken(jwt)
                .username(utilisateur.getUsername())
                .role("ROLE_USER")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Authentifier avec Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Utilisateur utilisateur = utilisateurRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        UserDetails userDetails = utilisateurDetailsService.loadUserByUsername(utilisateur.getUsername());
        String jwt = jwtService.generateToken(userDetails);

        // Révoquer les anciens tokens
        revokeAllUserTokens(utilisateur);

        // Sauvegarder le nouveau token
        saveToken(utilisateur, jwt);

        String role = utilisateur.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("ROLE_USER");

        return AuthResponse.builder()
                .accessToken(jwt)
                .username(utilisateur.getUsername())
                .role(role)
                .build();
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;

        String jwt = authHeader.substring(7);
        tokenRepository.findByToken(jwt).ifPresent(token -> {
            token.setRevoked(true);
            tokenRepository.save(token);
        });
    }

    private void saveToken(Utilisateur utilisateur, String jwt) {
        Token token = Token.builder()
                .token(jwt)
                .utilisateur(utilisateur)
                .revoked(false)
                .expiredDate(new Date(System.currentTimeMillis() + 86400000))
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(Utilisateur utilisateur) {
        var tokens = tokenRepository.findByUtilisateurIdAndRevokedFalse(utilisateur.getId());
        tokens.forEach(t -> t.setRevoked(true));
        tokenRepository.saveAll(tokens);
    }
}