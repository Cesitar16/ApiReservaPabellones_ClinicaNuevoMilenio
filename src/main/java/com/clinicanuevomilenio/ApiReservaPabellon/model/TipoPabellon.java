package com.clinicanuevomilenio.ApiReservaPabellon.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TIPO_PABELLON")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoPabellon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_pabellon")
    private Integer id;

    private String nombre;
    private String descripcion;
}