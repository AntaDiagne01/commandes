package com.polytech.commandes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneCommandeResponse {
    private Long id;
    private ProduitResponse produit;
    private Integer quantite;
    private BigDecimal prixUnitaire;
}