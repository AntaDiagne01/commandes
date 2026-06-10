package com.polytech.commandes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProduitResponse {
    private Long id;
    private String nom;
    private BigDecimal prix;
    private Integer stock;
}