package com.clinicanuevomilenio.ApiReservaPabellon.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "SOLICITUD_RESERVA") // <-- Coincide con el nombre de tu tabla
@Data
public class ReservaPabellon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_id") // Coincide con la columna de la BD
    private Integer id;

    @Column(name = "fecha_solicitud") // Coincide con la columna de la BD
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_confirmacion") // Coincide con la columna de la BD
    private LocalDateTime fechaConfirmacion;

    @Column(name = "fecha_hr_inicio") // Coincide con la columna de la BD
    private LocalDateTime fechaHrInicio;

    @Column(name = "fecha_hr_termino") // Coincide con la columna de la BD
    private LocalDateTime fechaHrTermino;

    @Column(name = "estado_id") // Coincide con la columna de la BD
    private Integer estadoId;

    @Column(name = "motivo") // Coincide con la columna de la BD
    private String motivo;

    @Column(name = "urgencia") // Coincide con la columna de la BD
    private Boolean urgencia;

    @Column(name = "comentario") // Coincide con la columna de la BD
    private String comentario;

    @Column(name = "PABELLON_pabellon_id") // Coincide con la columna de la BD
    private Integer pabellonId;

    @Column(name = "USUARIO_id_usuario") // Coincide con la columna de la BD
    private Integer usuarioId;
}
