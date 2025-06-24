package com.clinicanuevomilenio.ApiReservaPabellon.services;

import com.clinicanuevomilenio.ApiReservaPabellon.dto.*;
import com.clinicanuevomilenio.ApiReservaPabellon.model.EstadoSolicitud;
import com.clinicanuevomilenio.ApiReservaPabellon.model.ReservaPabellon;
import com.clinicanuevomilenio.ApiReservaPabellon.repository.EstadoSolicitudRepository;
import com.clinicanuevomilenio.ApiReservaPabellon.repository.ReservaPabellonRepository;
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
    private PabellonClientService pabellonClient;

    @Autowired
    private UsuarioClientService usuarioClient;

    public ReservaPabellonResponseDTO crearReserva(ReservaPabellonRequestDTO dto, Integer userId) {
        PabellonDTO pabellon = pabellonClient.obtenerPabellonPorId(dto.getPabellonId());

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

        EstadoSolicitud estado = estadoRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Estado inicial no encontrado"));
        reserva.setEstadoId(estado.getId());

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

        EstadoSolicitud estado = estadoRepository.findById(reserva.getEstadoId())
                .orElseThrow(() -> new RuntimeException("EstadoSolicitud no encontrada"));
        EstadoSolicitudDTO estadoDTO = new EstadoSolicitudDTO();
        estadoDTO.setId(estado.getId());
        estadoDTO.setNombre(estado.getNombre());
        dto.setEstado(estadoDTO);

        UsuarioDTO usuario = usuarioClient.obtenerUsuarioPorId(reserva.getUsuarioId());
        dto.setUsuario(usuario);

        PabellonDTO pabellon = pabellonClient.obtenerPabellonPorId(reserva.getPabellonId());
        dto.setPabellon(pabellon); // Se inyecta completo con estado y tipo anidados

        return dto;
    }

    public List<ReservaPabellonResponseDTO> buscarReservasPorEstadoYTipoDePabellon(Integer estadoId, Integer tipoId) {
        List<Integer> ids = pabellonClient.obtenerIdsPabellonesPorEstadoYTipo(estadoId, tipoId);
        if (ids.isEmpty()) return List.of();

        return reservaRepository.findByPabellonIdIn(ids).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaPabellonResponseDTO> listarPorEstadoDePabellon(Integer estadoId) {
        List<Integer> ids = pabellonClient.obtenerIdsPorEstado(estadoId);
        if (ids.isEmpty()) return List.of();

        return reservaRepository.findByPabellonIdIn(ids).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}