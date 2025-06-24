package com.clinicanuevomilenio.ApiReservaPabellon.repository;

import com.clinicanuevomilenio.ApiReservaPabellon.model.ReservaPabellon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaPabellonRepository extends JpaRepository<ReservaPabellon, Integer> {

    List<ReservaPabellon> findByPabellonIdIn(List<Integer> pabellonIds);
}