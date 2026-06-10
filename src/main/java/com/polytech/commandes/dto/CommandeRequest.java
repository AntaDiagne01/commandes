package com.polytech.commandes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CommandeRequest {

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    private List<LigneCommandeRequest> lignes;
}