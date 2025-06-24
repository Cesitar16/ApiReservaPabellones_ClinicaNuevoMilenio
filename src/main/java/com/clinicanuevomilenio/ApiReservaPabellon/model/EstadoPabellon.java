package com.clinicanuevomilenio.ApiReservaPabellon.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ESTADO_PABELLON")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPabellon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer id;

    private String nombre;
    private String descripcion;
}