package com.clinicanuevomilenio.ApiReservaPabellon.services;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
            throw new RuntimeException("Usuario no encontrado con ID: " + idUsuario);
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el servicio de usuarios: " + e.getMessage());
        }
    }
}