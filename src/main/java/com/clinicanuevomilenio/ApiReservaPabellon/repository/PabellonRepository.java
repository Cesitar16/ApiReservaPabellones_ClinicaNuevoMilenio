package com.clinicanuevomilenio.ApiReservaPabellon.repository;

import com.clinicanuevomilenio.ApiReservaPabellon.model.Pabellon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PabellonRepository extends JpaRepository<Pabellon, Integer> {
}