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
}