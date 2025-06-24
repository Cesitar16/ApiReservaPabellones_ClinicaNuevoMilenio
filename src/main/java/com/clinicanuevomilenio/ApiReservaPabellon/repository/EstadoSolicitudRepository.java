package com.clinicanuevomilenio.ApiReservaPabellon.repository;

import com.clinicanuevomilenio.ApiReservaPabellon.model.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitud, Integer> {
}