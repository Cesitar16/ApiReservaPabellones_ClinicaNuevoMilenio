package com.clinicanuevomilenio.ApiReservaPabellon.services;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.PabellonDTO;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
            throw new RuntimeException("Pabellón no encontrado con ID: " + id);
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el servicio de pabellones: " + e.getMessage());
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