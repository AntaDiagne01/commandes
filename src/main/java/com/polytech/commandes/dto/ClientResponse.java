package com.polytech.commandes.dto;

import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String nom;
    private String email;
}