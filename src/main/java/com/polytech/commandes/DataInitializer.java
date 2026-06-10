package com.polytech.commandes;

import com.polytech.commandes.entity.Client;
import com.polytech.commandes.entity.Role;
import com.polytech.commandes.entity.Role.RoleEnum;
import com.polytech.commandes.entity.Utilisateur;
import com.polytech.commandes.repository.ClientRepository;
import com.polytech.commandes.repository.RoleRepository;
import com.polytech.commandes.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Profile("dev")  // actif uniquement en profil dev
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 1. Créer les rôles s'ils n'existent pas
        Role roleAdmin = roleRepository.findByName(RoleEnum.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(RoleEnum.ROLE_ADMIN).build()));

        Role roleUser = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(RoleEnum.ROLE_USER).build()));

        // 2. Créer l'admin s'il n'existe pas
        if (utilisateurRepository.findByUsername("admin").isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(roleAdmin);

            Utilisateur admin = Utilisateur.builder()
                    .username("admin")
                    .email("admin@polytech.sn")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .roles(roles)
                    .build();

            admin = utilisateurRepository.save(admin);
            log.info("✅ Utilisateur admin créé");

            // Créer le client associé
            Client clientAdmin = Client.builder()
                    .nom("Administrateur")
                    .email("admin@polytech.sn")
                    .utilisateur(admin)
                    .build();
            clientRepository.save(clientAdmin);
        }

        // 3. Créer quelques clients de test
        if (clientRepository.count() <= 1) {
            Utilisateur user1 = Utilisateur.builder()
                    .username("user1")
                    .email("user1@test.sn")
                    .password(passwordEncoder.encode("user123"))
                    .enabled(true)
                    .roles(Set.of(roleUser))
                    .build();
            user1 = utilisateurRepository.save(user1);

            clientRepository.save(Client.builder()
                    .nom("Moussa Diallo")
                    .email("user1@test.sn")
                    .utilisateur(user1)
                    .build());

            Utilisateur user2 = Utilisateur.builder()
                    .username("user2")
                    .email("user2@test.sn")
                    .password(passwordEncoder.encode("user123"))
                    .enabled(true)
                    .roles(Set.of(roleUser))
                    .build();
            user2 = utilisateurRepository.save(user2);

            clientRepository.save(Client.builder()
                    .nom("Fatou Sow")
                    .email("user2@test.sn")
                    .utilisateur(user2)
                    .build());

            log.info("Clients de test créés");
        }
    }
}