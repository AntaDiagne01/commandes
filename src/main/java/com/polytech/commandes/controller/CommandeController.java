package com.polytech.commandes.controller;

import com.polytech.commandes.dto.CommandeRequest;
import com.polytech.commandes.dto.CommandeResponse;
import com.polytech.commandes.service.CommandeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
@Tag(name = "Commandes", description = "Gestion des commandes")
@SecurityRequirement(name = "bearerAuth")
public class CommandeController {

    private final CommandeService commandeService;

    @GetMapping
    @Operation(summary = "Lister toutes les commandes")
    public ResponseEntity<List<CommandeResponse>> findAll() {
        return ResponseEntity.ok(commandeService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une commande par son ID")
    public ResponseEntity<CommandeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.findById(id));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Obtenir les commandes d'un client")
    public ResponseEntity<List<CommandeResponse>> findByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(commandeService.findByClientId(clientId));
    }

    @PostMapping
    @Operation(summary = "Créer une commande")
    public ResponseEntity<CommandeResponse> create(@Valid @RequestBody CommandeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandeService.create(request));
    }

    @PatchMapping("/{id}/valider")
    @Operation(summary = "Valider une commande")
    public ResponseEntity<CommandeResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.valider(id));
    }

    @PatchMapping("/{id}/annuler")
    @Operation(summary = "Annuler une commande")
    public ResponseEntity<CommandeResponse> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.annuler(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une commande")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}