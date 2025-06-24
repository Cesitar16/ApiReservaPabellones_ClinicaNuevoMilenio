package com.clinicanuevomilenio.ApiReservaPabellon.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservaPabellonRequestDTO {

    private LocalDateTime fechaHrInicio;
    private LocalDateTime fechaHrTermino;
    private String motivo;
    private Boolean urgencia;
    private String comentario;

    private Integer pabellonId;
}