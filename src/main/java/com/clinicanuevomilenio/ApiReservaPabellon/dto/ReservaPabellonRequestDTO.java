package com.clinicanuevomilenio.ApiReservaPabellon.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaPabellonRequestDTO {

    @NotNull(message = "Debe ingresar la fecha de inicio")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime fechaHrInicio;

    @NotNull(message = "Debe ingresar la fecha de término")
    @Future(message = "La fecha de término debe ser futura")
    private LocalDateTime fechaHrTermino;

    @NotBlank(message = "El motivo no puede estar vacío")
    private String motivo;

    @NotNull
    private Boolean urgencia;

    private String comentario; // opcional

    @NotNull
    private Integer pabellonId;
}