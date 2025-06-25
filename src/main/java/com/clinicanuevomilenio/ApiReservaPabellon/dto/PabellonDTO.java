package com.clinicanuevomilenio.ApiReservaPabellon.dto;

import lombok.Data;

@Data
public class PabellonDTO {
    private Integer id;
    private String nombre;
    private EstadoDTO estado;
    private TipoDTO tipoPabellon;
}