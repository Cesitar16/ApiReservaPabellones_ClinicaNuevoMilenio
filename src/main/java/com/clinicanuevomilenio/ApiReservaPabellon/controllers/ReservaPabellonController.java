package com.clinicanuevomilenio.ApiReservaPabellon.controllers;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.ReservaPabellonRequestDTO;
import com.clinicanuevomilenio.ApiReservaPabellon.dto.ReservaPabellonResponseDTO;
import com.clinicanuevomilenio.ApiReservaPabellon.services.ReservaPabellonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaPabellonController {

    @Autowired
    private ReservaPabellonService reservaService;

    // Este endpoint es donde se creará una nueva reserva
    @PostMapping
    public ResponseEntity<ReservaPabellonResponseDTO> crearReserva(
            @RequestBody ReservaPabellonRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Integer userId) {

        ReservaPabellonResponseDTO response = reservaService.crearReserva(requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    // Para ver todas las reservas creadas
    @GetMapping
    public ResponseEntity<List<ReservaPabellonResponseDTO>> listarReservas() {
        return ResponseEntity.ok(reservaService.listarReservas());
    }

    // Para ver una reserva específica por su ID
    @GetMapping("/{id}")
    public ResponseEntity<ReservaPabellonResponseDTO> obtenerReserva(@PathVariable Integer id) {
        return ResponseEntity.ok(reservaService.obtenerReservaPorId(id));
    }
}