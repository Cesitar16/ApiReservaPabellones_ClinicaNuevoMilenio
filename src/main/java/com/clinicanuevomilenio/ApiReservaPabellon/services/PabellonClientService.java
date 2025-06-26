package com.clinicanuevomilenio.ApiReservaPabellon.services;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.PabellonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PabellonClientService {

    @Autowired
    @Qualifier("pabellonClient")
    private WebClient webClient;

    public PabellonDTO obtenerPabellonPorId(Integer id) {
        try {
            return webClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .bodyToMono(PabellonDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el servicio de pabellones: " + e.getMessage());
        }
    }

    // --- NUEVO MÉTODO PARA BÚSQUEDA MASIVA ---
    /**
     * Llama al endpoint /por-ids de la pabellones-api para obtener
     * una lista de pabellones en una sola llamada de red.
     * @param ids La lista de IDs de pabellones a buscar.
     * @return Una lista de DTOs de los pabellones encontrados.
     */
    public List<PabellonDTO> obtenerPabellonesPorIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        // Usamos .join(",") para asegurar que los IDs se envíen como "1,2,3"
        String idsComoString = ids.stream().map(String::valueOf).collect(Collectors.joining(","));

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/por-ids")
                            .queryParam("ids", idsComoString) // Enviamos el string separado por comas
                            .build())
                    .retrieve()
                    .bodyToFlux(PabellonDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Error al obtener pabellones por IDs: " + e.getMessage());
            return List.of();
        }
    }

    public List<Integer> obtenerIdsPabellonesPorEstadoYTipo(Integer estadoId, Integer tipoId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/filtro")
                            .queryParam("estadoId", estadoId)
                            .queryParam("tipoId", tipoId)
                            .build())
                    .retrieve()
                    .bodyToFlux(PabellonDTO.class)
                    .map(PabellonDTO::getId)
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener pabellones filtrados: " + e.getMessage());
        }
    }

    public List<Integer> obtenerIdsPorEstado(Integer estadoId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/estado")
                            .queryParam("estadoId", estadoId)
                            .build())
                    .retrieve()
                    .bodyToFlux(PabellonDTO.class)
                    .map(PabellonDTO::getId)
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener pabellones por estado: " + e.getMessage());
        }
    }
}