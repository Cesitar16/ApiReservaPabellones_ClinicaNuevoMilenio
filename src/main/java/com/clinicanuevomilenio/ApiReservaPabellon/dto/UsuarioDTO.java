package com.clinicanuevomilenio.ApiReservaPabellon.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Integer idUsuario;
    private String username;
    private String nombreCompleto; // puede ser null si la otra API no lo entrega
}