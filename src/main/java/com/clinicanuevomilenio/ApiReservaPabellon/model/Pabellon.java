package com.clinicanuevomilenio.ApiReservaPabellon.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PABELLON")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pabellon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pabellon_id")
    private Integer id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoPabellon estado;

    @ManyToOne
    @JoinColumn(name = "TIPO_PABELLON_id_tipo_pabellon", nullable = false)
    private TipoPabellon tipoPabellon;
}