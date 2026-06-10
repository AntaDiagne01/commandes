package com.polytech.commandes.service;

import com.polytech.commandes.dto.ClientRequest;
import com.polytech.commandes.dto.ClientResponse;
import com.polytech.commandes.entity.Client;
import com.polytech.commandes.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<ClientResponse> findAll() {
        return clientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé avec l'id : " + id));
        return toResponse(client);
    }

    public ClientResponse create(ClientRequest request) {
        Client client = Client.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .build();
        return toResponse(clientRepository.save(client));
    }

    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé avec l'id : " + id));
        client.setNom(request.getNom());
        client.setEmail(request.getEmail());
        return toResponse(clientRepository.save(client));
    }

    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new EntityNotFoundException("Client non trouvé avec l'id : " + id);
        }
        clientRepository.deleteById(id);
    }

    public ClientResponse toResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setNom(client.getNom());
        response.setEmail(client.getEmail());
        return response;
    }
}