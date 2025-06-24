package com.clinicanuevomilenio.ApiReservaPabellon.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ESTADO_SOLICITUD")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer id;

    private String nombre;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;
}