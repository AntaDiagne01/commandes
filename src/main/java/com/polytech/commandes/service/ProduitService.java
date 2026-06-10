package com.polytech.commandes.service;

import com.polytech.commandes.dto.ProduitRequest;
import com.polytech.commandes.dto.ProduitResponse;
import com.polytech.commandes.entity.Produit;
import com.polytech.commandes.repository.ProduitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;

    public List<ProduitResponse> findAll() {
        return produitRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProduitResponse findById(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'id : " + id));
        return toResponse(produit);
    }

    public ProduitResponse create(ProduitRequest request) {
        Produit produit = Produit.builder()
                .nom(request.getNom())
                .prix(request.getPrix())
                .stock(request.getStock())
                .build();
        return toResponse(produitRepository.save(produit));
    }

    public ProduitResponse update(Long id, ProduitRequest request) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'id : " + id));
        produit.setNom(request.getNom());
        produit.setPrix(request.getPrix());
        produit.setStock(request.getStock());
        return toResponse(produitRepository.save(produit));
    }

    public void delete(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new EntityNotFoundException("Produit non trouvé avec l'id : " + id);
        }
        produitRepository.deleteById(id);
    }

    // Méthode utilitaire : Entité → DTO Response
    public ProduitResponse toResponse(Produit produit) {
        ProduitResponse response = new ProduitResponse();
        response.setId(produit.getId());
        response.setNom(produit.getNom());
        response.setPrix(produit.getPrix());
        response.setStock(produit.getStock());
        return response;
    }
}