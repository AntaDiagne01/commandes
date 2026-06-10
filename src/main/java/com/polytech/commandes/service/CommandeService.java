package com.polytech.commandes.service;

import com.polytech.commandes.dto.*;
import com.polytech.commandes.entity.*;
import com.polytech.commandes.entity.Commande.StatutCommande;
import com.polytech.commandes.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final ProduitService produitService;
    private final ClientService clientService;

    public List<CommandeResponse> findAll() {
        return commandeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CommandeResponse findById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'id : " + id));
        return toResponse(commande);
    }

    public List<CommandeResponse> findByClientId(Long clientId) {
        return commandeRepository.findByClientId(clientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommandeResponse create(CommandeRequest request) {
        // 1. Vérifier que le client existe
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé avec l'id : " + request.getClientId()));

        // 2. Créer la commande
        Commande commande = Commande.builder()
                .client(client)
                .build();

        // 3. Ajouter les lignes
        if (request.getLignes() != null) {
            for (LigneCommandeRequest ligneReq : request.getLignes()) {
                Produit produit = produitRepository.findById(ligneReq.getProduitId())
                        .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'id : " + ligneReq.getProduitId()));

                LigneCommande ligne = LigneCommande.builder()
                        .commande(commande)
                        .produit(produit)
                        .quantite(ligneReq.getQuantite())
                        .prixUnitaire(produit.getPrix()) // prix au moment de la commande
                        .build();

                commande.getLignes().add(ligne);
            }
        }

        return toResponse(commandeRepository.save(commande));
    }

    @Transactional
    public CommandeResponse valider(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'id : " + id));

        // Règle métier : on ne peut pas modifier une commande VALIDATED ou CANCELLED
        if (commande.getStatus() != StatutCommande.CREATED) {
            throw new IllegalStateException("Impossible de valider une commande avec le statut : " + commande.getStatus());
        }

        // Règle métier : vérifier le stock pour chaque ligne
        for (LigneCommande ligne : commande.getLignes()) {
            Produit produit = ligne.getProduit();
            if (produit.getStock() < ligne.getQuantite()) {
                throw new IllegalStateException(
                        "Stock insuffisant pour le produit '" + produit.getNom() +
                                "' (stock: " + produit.getStock() + ", demandé: " + ligne.getQuantite() + ")"
                );
            }
        }

        // Décrémenter le stock après validation
        for (LigneCommande ligne : commande.getLignes()) {
            Produit produit = ligne.getProduit();
            produit.setStock(produit.getStock() - ligne.getQuantite());
            produitRepository.save(produit);
        }

        commande.setStatus(StatutCommande.VALIDATED);
        return toResponse(commandeRepository.save(commande));
    }

    @Transactional
    public CommandeResponse annuler(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'id : " + id));

        if (commande.getStatus() != StatutCommande.CREATED) {
            throw new IllegalStateException("Impossible d'annuler une commande avec le statut : " + commande.getStatus());
        }

        commande.setStatus(StatutCommande.CANCELLED);
        return toResponse(commandeRepository.save(commande));
    }

    public void delete(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'id : " + id));

        if (commande.getStatus() == StatutCommande.VALIDATED) {
            throw new IllegalStateException("Impossible de supprimer une commande validée");
        }

        commandeRepository.deleteById(id);
    }

    // Entité → DTO Response
    public CommandeResponse toResponse(Commande commande) {
        CommandeResponse response = new CommandeResponse();
        response.setId(commande.getId());
        response.setDateCommande(commande.getDateCommande());
        response.setStatus(commande.getStatus());
        response.setClient(clientService.toResponse(commande.getClient()));

        List<LigneCommandeResponse> lignes = commande.getLignes().stream()
                .map(ligne -> {
                    LigneCommandeResponse lr = new LigneCommandeResponse();
                    lr.setId(ligne.getId());
                    lr.setProduit(produitService.toResponse(ligne.getProduit()));
                    lr.setQuantite(ligne.getQuantite());
                    lr.setPrixUnitaire(ligne.getPrixUnitaire());
                    return lr;
                })
                .collect(Collectors.toList());

        response.setLignes(lignes);

        // Calcul du total
        BigDecimal total = lignes.stream()
                .map(l -> l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotal(total);

        return response;
    }
}