package com.clinicanuevomilenio.ApiReservaPabellon.controllers;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.ReservaModificacionDTO;
import com.clinicanuevomilenio.ApiReservaPabellon.dto.ReservaPabellonRequestDTO;
import com.clinicanuevomilenio.ApiReservaPabellon.dto.ReservaPabellonResponseDTO;
import com.clinicanuevomilenio.ApiReservaPabellon.services.ReservaPabellonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @GetMapping("/")
    public ResponseEntity<List<ReservaPabellonResponseDTO>> listarReservas() {
        return ResponseEntity.ok(reservaService.listarReservas());
    }

    // Para ver una reserva específica por su ID
    @GetMapping("/{id}")
    public ResponseEntity<ReservaPabellonResponseDTO> obtenerReserva(@PathVariable Integer id) {
        return ResponseEntity.ok(reservaService.obtenerReservaPorId(id));
    }

    @GetMapping("/pabellones/estado")
    public ResponseEntity<List<ReservaPabellonResponseDTO>> obtenerReservasPorEstadoPabellon(
            @RequestParam Integer estadoId) {
        List<ReservaPabellonResponseDTO> reservas = reservaService.listarPorEstadoDePabellon(estadoId);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping
    public ResponseEntity<List<ReservaPabellonResponseDTO>> listarReservasPorUsuario(
            @RequestHeader("X-User-Id") Integer userId) { // <-- AÑADIMOS ESTA CABECERA

        // Llamamos al nuevo método del servicio, pasándole el ID del usuario
        List<ReservaPabellonResponseDTO> misReservas = reservaService.listarReservasPorUsuario(userId);

        return ResponseEntity.ok(misReservas);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(
            @PathVariable Integer id,
            @RequestHeader("X-User-Id") Integer userId) {
        try {
            ReservaPabellonResponseDTO reservaCancelada = reservaService.cancelarReserva(id, userId);
            return ResponseEntity.ok(reservaCancelada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarReserva(
            @PathVariable Integer id,
            @RequestBody ReservaModificacionDTO dto,
            @RequestHeader("X-User-Id") Integer userId) {
        try {
            ReservaPabellonResponseDTO reservaModificada = reservaService.modificarReserva(id, dto, userId);
            return ResponseEntity.ok(reservaModificada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/aprobadas/por-usuario")
    public ResponseEntity<List<ReservaPabellonResponseDTO>> listarReservasAprobadasPorUsuario(
            @RequestHeader("X-User-Id") Integer userId) {

        List<ReservaPabellonResponseDTO> reservasAprobadas = reservaService.listarReservasAprobadasPorUsuario(userId);
        return ResponseEntity.ok(reservasAprobadas);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarReserva(
            @PathVariable Integer id,
            @RequestHeader("X-User-Id") Integer userId) {
        try {
            ReservaPabellonResponseDTO reservaFinalizada = reservaService.finalizarReserva(id, userId);
            return ResponseEntity.ok(reservaFinalizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}