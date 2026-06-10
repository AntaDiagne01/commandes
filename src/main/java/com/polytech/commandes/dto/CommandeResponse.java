package com.polytech.commandes.dto;

import com.polytech.commandes.entity.Commande.StatutCommande;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommandeResponse {
    private Long id;
    private LocalDateTime dateCommande;
    private StatutCommande status;
    private ClientResponse client;
    private List<LigneCommandeResponse> lignes;
    private java.math.BigDecimal total;
}