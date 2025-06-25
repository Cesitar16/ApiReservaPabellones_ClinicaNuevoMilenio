package com.clinicanuevomilenio.ApiReservaPabellon.services;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.*;
import com.clinicanuevomilenio.ApiReservaPabellon.model.EstadoSolicitud;
import com.clinicanuevomilenio.ApiReservaPabellon.model.ReservaPabellon;
import com.clinicanuevomilenio.ApiReservaPabellon.repository.EstadoSolicitudRepository;
import com.clinicanuevomilenio.ApiReservaPabellon.repository.ReservaPabellonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReservaPabellonService {

    @Autowired
    private ReservaPabellonRepository reservaRepository;

    @Autowired
    private EstadoSolicitudRepository estadoRepository;

    @Autowired
    private PabellonClientService pabellonClient;

    @Autowired
    private UsuarioClientService usuarioClient;

    @Transactional
    public ReservaPabellonResponseDTO crearReserva(ReservaPabellonRequestDTO dto, Integer userId) {
        PabellonDTO pabellon = pabellonClient.obtenerPabellonPorId(dto.getPabellonId());
        if (pabellon == null || !"Disponible".equals(pabellon.getEstado().getNombre())) {
            throw new RuntimeException("Reserva rechazada: El pabellón no existe o no se encuentra 'Disponible'.");
        }

        List<ReservaPabellon> reservasSuperpuestas = reservaRepository.findOverlappingReservas(
                dto.getPabellonId(), dto.getFechaHrInicio(), dto.getFechaHrTermino());

        if (!reservasSuperpuestas.isEmpty()) {
            throw new RuntimeException("Reserva rechazada: Ya existe una reserva en el horario solicitado para este pabellón.");
        }

        ReservaPabellon reserva = new ReservaPabellon();
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setFechaConfirmacion(null);
        reserva.setFechaHrInicio(dto.getFechaHrInicio());
        reserva.setFechaHrTermino(dto.getFechaHrTermino());
        reserva.setMotivo(dto.getMotivo());
        reserva.setUrgencia(dto.getUrgencia());
        reserva.setComentario(dto.getComentario());
        reserva.setPabellonId(pabellon.getId());
        reserva.setUsuarioId(userId);

        EstadoSolicitud estado = estadoRepository.findById(1).orElseThrow(() -> new RuntimeException("Estado inicial 'Pendiente' no encontrado."));
        reserva.setEstadoId(estado.getId());

        ReservaPabellon reservaGuardada = reservaRepository.save(reserva);

        UsuarioDTO usuario = usuarioClient.obtenerUsuarioPorId(userId);

        return toResponseDTO(reservaGuardada, usuario, pabellon);
    }

    @Transactional(readOnly = true)
    public List<ReservaPabellonResponseDTO> listarReservas() {
        List<ReservaPabellon> reservas = reservaRepository.findAll();
        if (reservas.isEmpty()) return List.of();

        List<Integer> usuarioIds = reservas.stream().map(ReservaPabellon::getUsuarioId).distinct().toList();
        List<Integer> pabellonIds = reservas.stream().map(ReservaPabellon::getPabellonId).distinct().toList();

        Map<Integer, UsuarioDTO> usuariosMap = usuarioClient.obtenerUsuariosPorIds(usuarioIds).stream()
                .collect(Collectors.toMap(UsuarioDTO::getIdUsuario, Function.identity()));

        Map<Integer, PabellonDTO> pabellonesMap = pabellonClient.obtenerPabellonesPorIds(pabellonIds).stream()
                .collect(Collectors.toMap(PabellonDTO::getId, Function.identity()));

        return reservas.stream().map(reserva -> {
            UsuarioDTO usuario = usuariosMap.get(reserva.getUsuarioId());
            PabellonDTO pabellon = pabellonesMap.get(reserva.getPabellonId());
            return toResponseDTO(reserva, usuario, pabellon);
        }).collect(Collectors.toList());
    }

    // --- NUEVO MÉTODO AÑADIDO ---
    @Transactional(readOnly = true)
    public ReservaPabellonResponseDTO obtenerReservaPorId(Integer id) {
        ReservaPabellon reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
        return toResponseDTO(reserva);
    }

    private ReservaPabellonResponseDTO toResponseDTO(ReservaPabellon reserva, UsuarioDTO usuario, PabellonDTO pabellon) {
        ReservaPabellonResponseDTO dto = new ReservaPabellonResponseDTO();
        dto.setId(reserva.getId());
        dto.setFechaSolicitud(reserva.getFechaSolicitud());
        dto.setFechaConfirmacion(reserva.getFechaConfirmacion());
        dto.setFechaHrInicio(reserva.getFechaHrInicio());
        dto.setFechaHrTermino(reserva.getFechaHrTermino());
        dto.setMotivo(reserva.getMotivo());
        dto.setUrgencia(reserva.getUrgencia());
        dto.setComentario(reserva.getComentario());

        EstadoSolicitud estado = estadoRepository.findById(reserva.getEstadoId())
                .orElseThrow(() -> new RuntimeException("EstadoSolicitud no encontrada"));
        EstadoSolicitudDTO estadoDTO = new EstadoSolicitudDTO();
        estadoDTO.setId(estado.getId());
        estadoDTO.setNombre(estado.getNombre());
        dto.setEstado(estadoDTO);
        dto.setUsuario(usuario);
        dto.setPabellon(pabellon);
        return dto;
    }

    private ReservaPabellonResponseDTO toResponseDTO(ReservaPabellon reserva) {
        UsuarioDTO usuario = usuarioClient.obtenerUsuarioPorId(reserva.getUsuarioId());
        PabellonDTO pabellon = pabellonClient.obtenerPabellonPorId(reserva.getPabellonId());
        return toResponseDTO(reserva, usuario, pabellon);
    }

    @Transactional(readOnly = true)
    public List<ReservaPabellonResponseDTO> buscarReservasPorEstadoYTipoDePabellon(Integer estadoId, Integer tipoId) {
        List<Integer> ids = pabellonClient.obtenerIdsPabellonesPorEstadoYTipo(estadoId, tipoId);
        if (ids.isEmpty()) return List.of();

        return reservaRepository.findByPabellonIdIn(ids).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // --- NUEVO MÉTODO AÑADIDO ---
    @Transactional(readOnly = true)
    public List<ReservaPabellonResponseDTO> listarPorEstadoDePabellon(Integer estadoId) {
        List<Integer> pabellonIds = pabellonClient.obtenerIdsPorEstado(estadoId);
        if (pabellonIds.isEmpty()) return List.of();

        List<ReservaPabellon> reservas = reservaRepository.findByPabellonIdIn(pabellonIds);
        if (reservas.isEmpty()) return List.of();

        List<Integer> usuarioIds = reservas.stream().map(ReservaPabellon::getUsuarioId).distinct().toList();

        Map<Integer, UsuarioDTO> usuariosMap = usuarioClient.obtenerUsuariosPorIds(usuarioIds).stream()
                .collect(Collectors.toMap(UsuarioDTO::getIdUsuario, Function.identity()));

        Map<Integer, PabellonDTO> pabellonesMap = pabellonClient.obtenerPabellonesPorIds(pabellonIds).stream()
                .collect(Collectors.toMap(PabellonDTO::getId, Function.identity()));

        return reservas.stream().map(reserva -> {
            UsuarioDTO usuario = usuariosMap.get(reserva.getUsuarioId());
            PabellonDTO pabellon = pabellonesMap.get(reserva.getPabellonId());
            return toResponseDTO(reserva, usuario, pabellon);
        }).collect(Collectors.toList());
    }
}