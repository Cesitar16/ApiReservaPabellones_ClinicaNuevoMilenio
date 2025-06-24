package com.clinicanuevomilenio.ApiReservaPabellon.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaPabellonResponseDTO {

    private Integer id;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaHrInicio;
    private LocalDateTime fechaHrTermino;
    private String motivo;
    private Boolean urgencia;
    private String comentario;

    private EstadoSolicitudDTO estado;
    private PabellonDTO pabellon;
    private UsuarioDTO usuario;
}