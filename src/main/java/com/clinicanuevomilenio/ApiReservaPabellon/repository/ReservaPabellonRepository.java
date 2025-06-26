package com.clinicanuevomilenio.ApiReservaPabellon.repository;

import com.clinicanuevomilenio.ApiReservaPabellon.model.ReservaPabellon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaPabellonRepository extends JpaRepository<ReservaPabellon, Integer> {

    List<ReservaPabellon> findByPabellonIdIn(List<Integer> pabellonIds);

    @Query("SELECT r FROM ReservaPabellon r WHERE r.pabellonId = :pabellonId " +
            "AND r.fechaHrInicio < :finNuevo " +
            "AND r.fechaHrTermino > :inicioNuevo")
    List<ReservaPabellon> findOverlappingReservas(
            @Param("pabellonId") Integer pabellonId,
            @Param("inicioNuevo") LocalDateTime inicioNuevo,
            @Param("finNuevo") LocalDateTime finNuevo);

    List<ReservaPabellon> findByUsuarioId(Integer usuarioId);

    @Query("SELECT r FROM ReservaPabellon r WHERE r.pabellonId = :pabellonId " +
            "AND r.id != :reservaIdAExcluir " + // <-- La clave es excluir la reserva actual
            "AND r.estadoId NOT IN (4, 5) " + // No considerar las finalizadas o canceladas
            "AND (r.fechaHrInicio < :fechaHrTermino AND r.fechaHrTermino > :fechaHrInicio)")
    List<ReservaPabellon> findOverlappingReservasExcluyendoActual(
            @Param("pabellonId") Integer pabellonId,
            @Param("fechaHrInicio") LocalDateTime fechaHrInicio,
            @Param("fechaHrTermino") LocalDateTime fechaHrTermino,
            @Param("reservaIdAExcluir") Integer reservaIdAExcluir
    );

    List<ReservaPabellon> findByUsuarioIdAndEstadoId(Integer usuarioId, Integer estadoId);
}