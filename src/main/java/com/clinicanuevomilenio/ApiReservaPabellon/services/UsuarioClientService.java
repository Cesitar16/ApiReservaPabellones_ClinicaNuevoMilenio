package com.clinicanuevomilenio.ApiReservaPabellon.services;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List; // Importar List

@Service
public class UsuarioClientService {

    @Autowired
    @Qualifier("usuarioClient")
    private WebClient webClient;

    public UsuarioDTO obtenerUsuarioPorId(Integer idUsuario) {
        try {
            return webClient.get()
                    .uri("/{id}", idUsuario)
                    .retrieve()
                    .bodyToMono(UsuarioDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            // Es mejor devolver null o un Optional.empty() para que el servicio que llama decida qué hacer.
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el servicio de usuarios: " + e.getMessage());
        }
    }

    // --- NUEVO MÉTODO PARA BÚSQUEDA MASIVA ---
    /**
     * Llama al endpoint /por-ids de la usuarios-api para obtener
     * una lista de usuarios en una sola llamada de red.
     * @param ids La lista de IDs de usuarios a buscar.
     * @return Una lista de DTOs de los usuarios encontrados.
     */
    public List<UsuarioDTO> obtenerUsuariosPorIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of(); // Devuelve una lista vacía si no hay IDs que buscar
        }

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/por-ids")
                            // Spring WebClient expandirá la lista de IDs a formato ?ids=1,2,3...
                            .queryParam("ids", ids)
                            .build())
                    .retrieve()
                    .bodyToFlux(UsuarioDTO.class) // Usamos bodyToFlux para recibir múltiples objetos
                    .collectList() // Los agrupamos en una lista
                    .block(); // Esperamos el resultado de forma síncrona
        } catch (Exception e) {
            // En caso de error, podrías devolver una lista vacía o lanzar una excepción
            System.err.println("Error al obtener usuarios por IDs: " + e.getMessage());
            return List.of();
        }
    }
}