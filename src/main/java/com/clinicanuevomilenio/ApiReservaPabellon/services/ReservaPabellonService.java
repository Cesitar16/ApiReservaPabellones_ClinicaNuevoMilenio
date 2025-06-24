package com.clinicanuevomilenio.ApiReservaPabellon.services;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.*;
import com.clinicanuevomilenio.ApiReservaPabellon.model.*;
import com.clinicanuevomilenio.ApiReservaPabellon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaPabellonService {

    @Autowired
    private ReservaPabellonRepository reservaRepository;

    @Autowired
    private EstadoSolicitudRepository estadoRepository;

    @Autowired
    private PabellonRepository pabellonRepository;

    public ReservaPabellonResponseDTO crearReserva(ReservaPabellonRequestDTO dto, Integer userId) {
        ReservaPabellon reserva = new ReservaPabellon();

        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setFechaHrInicio(dto.getFechaHrInicio());
        reserva.setFechaHrTermino(dto.getFechaHrTermino());
        reserva.setMotivo(dto.getMotivo());
        reserva.setUrgencia(dto.getUrgencia());
        reserva.setComentario(dto.getComentario());

        // Estado inicial (por ejemplo: ID 1 = PENDIENTE)
        EstadoSolicitud estado = estadoRepository.findById(1)
            .orElseThrow(() -> new RuntimeException("Estado inicial no encontrado"));
        reserva.setEstado(estado);

        // Pabellón desde el ID recibido
        Pabellon pabellon = pabellonRepository.findById(dto.getPabellonId())
            .orElseThrow(() -> new RuntimeException("Pabellón no encontrado"));
        reserva.setPabellon(pabellon);

        // Usuario autenticado (solo ID)
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        reserva.setUsuario(usuario);

        return toResponseDTO(reservaRepository.save(reserva));
    }

    public List<ReservaPabellonResponseDTO> listarReservas() {
        return reservaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ReservaPabellonResponseDTO obtenerReservaPorId(Integer id) {
        ReservaPabellon reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        return toResponseDTO(reserva);
    }

    // Transformación a DTO de respuesta
    private ReservaPabellonResponseDTO toResponseDTO(ReservaPabellon reserva) {
        ReservaPabellonResponseDTO dto = new ReservaPabellonResponseDTO();
        dto.setId(reserva.getId());
        dto.setFechaSolicitud(reserva.getFechaSolicitud());
        dto.setFechaConfirmacion(reserva.getFechaConfirmacion());
        dto.setFechaHrInicio(reserva.getFechaHrInicio());
        dto.setFechaHrTermino(reserva.getFechaHrTermino());
        dto.setMotivo(reserva.getMotivo());
        dto.setUrgencia(reserva.getUrgencia());
        dto.setComentario(reserva.getComentario());

        // Estado
        EstadoSolicitudDTO estadoDTO = new EstadoSolicitudDTO();
        estadoDTO.setId(reserva.getEstado().getId());
        estadoDTO.setNombre(reserva.getEstado().getNombre());
        dto.setEstado(estadoDTO);

        // Pabellón
        PabellonDTO pabellonDTO = new PabellonDTO();
        pabellonDTO.setId(reserva.getPabellon().getId());
        pabellonDTO.setNombre(reserva.getPabellon().getNombre());
        pabellonDTO.setEstadoPabellon(
            reserva.getPabellon().getEstado() != null ? reserva.getPabellon().getEstado().getNombre() : null
        );
        pabellonDTO.setTipoPabellon(
            reserva.getPabellon().getTipoPabellon() != null ? reserva.getPabellon().getTipoPabellon().getNombre() : null
        );
        dto.setPabellon(pabellonDTO);

        // Usuario (simplificado)
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(reserva.getUsuario().getId());
        usuarioDTO.setUsername(reserva.getUsuario().getUsername()); // puede ser null si no se cargó la relación
        dto.setUsuario(usuarioDTO);

        return dto;
    }
}