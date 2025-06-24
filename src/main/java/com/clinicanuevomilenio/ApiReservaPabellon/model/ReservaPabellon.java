package com.clinicanuevomilenio.ApiReservaPabellon.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SOLICITUD_RESERVA")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReservaPabellon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_id")
    private Integer id;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "fecha_hr_inicio", nullable = false)
    private LocalDateTime fechaHrInicio;

    @Column(name = "fecha_hr_termino", nullable = false)
    private LocalDateTime fechaHrTermino;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private Boolean urgencia;

    private String comentario;

    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoSolicitud estado;

    @ManyToOne
    @JoinColumn(name = "PABELLON_pabellon_id", nullable = false)
    private Pabellon pabellon;

    @ManyToOne
    @JoinColumn(name = "USUARIO_id_usuario", nullable = false)
    private Usuario usuario;
}