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
import java.util.Collections;
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
    public ReservaPabellonResponseDTO finalizarReserva(Integer reservaId, Integer solicitanteId) {
        // 1. Encontrar la reserva
        ReservaPabellon reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("No se encontró la reserva con ID: " + reservaId));

        // 2. Verificar permisos
        if (!reserva.getUsuarioId().equals(solicitanteId)) {
            throw new RuntimeException("No tiene permisos para finalizar esta reserva.");
        }

        // 3. Verificar estado: solo se puede finalizar una reserva que está "Aprobada"
        // Asumimos que el ID del estado "Aprobada" es 3.
        if (reserva.getEstadoId() != 3) {
            throw new RuntimeException("Solo se puede finalizar una reserva que esté 'En Proceso'.");
        }

        // 4. Cambiar el estado a "Completada"
        // Asumimos que el ID del estado "Completada" es 4, según tu script de ESTADO_SOLICITUD.
        EstadoSolicitud estadoCompletado = estadoRepository.findById(4)
                .orElseThrow(() -> new RuntimeException("El estado 'Completada' no se encuentra en la base de datos."));
        reserva.setEstadoId(estadoCompletado.getId());
        reserva.setFechaConfirmacion(LocalDateTime.now()); // Opcional: Usar este campo como fecha de finalización

        ReservaPabellon reservaFinalizada = reservaRepository.save(reserva);

        // Devolver la respuesta enriquecida
        return toResponseDTO(reservaFinalizada);
    }

    @Transactional(readOnly = true)
    public List<ReservaPabellonResponseDTO> listarReservasAprobadasPorUsuario(Integer usuarioId) {
        // Asumimos que el ID del estado "Aprobada" es 3, según tu script SQL.
        // Si es diferente, solo debes cambiar este número.
        final Integer ESTADO_APROBADA_ID = 3;

        // 1. Llama al nuevo método del repositorio.
        List<ReservaPabellon> reservasAprobadas = reservaRepository.findByUsuarioIdAndEstadoId(usuarioId, ESTADO_APROBADA_ID);

        if (reservasAprobadas.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Optimizamos las llamadas a otras APIs (reutilizamos la lógica que ya tienes)
        List<Integer> pabellonIds = reservasAprobadas.stream()
                .map(ReservaPabellon::getPabellonId)
                .distinct()
                .toList();

        UsuarioDTO usuario = usuarioClient.obtenerUsuarioPorId(usuarioId);
        Map<Integer, PabellonDTO> pabellonesMap = pabellonClient.obtenerPabellonesPorIds(pabellonIds).stream()
                .collect(Collectors.toMap(PabellonDTO::getId, Function.identity()));

        // 3. Construimos la respuesta
        return reservasAprobadas.stream().map(reserva -> {
            PabellonDTO pabellon = pabellonesMap.get(reserva.getPabellonId());
            return toResponseDTO(reserva, usuario, pabellon);
        }).collect(Collectors.toList());
    }

    @Transactional
    public ReservaPabellonResponseDTO modificarReserva(Integer reservaId, ReservaModificacionDTO dto, Integer solicitanteId) {
        // 1. Encontrar la reserva existente
        ReservaPabellon reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("No se encontró la reserva con ID: " + reservaId));

        // 2. Verificar permisos
        if (!reserva.getUsuarioId().equals(solicitanteId)) {
            throw new RuntimeException("No tiene permisos para modificar esta reserva.");
        }

        // 3. Verificar estado (no se puede modificar si ya está finalizada, rechazada o cancelada)
        if (List.of(3, 4, 5).contains(reserva.getEstadoId())) { // Asumiendo 3:Rechazada, 4:Finalizada, 5:Cancelada
            throw new RuntimeException("Esta reserva ya no puede ser modificada.");
        }

        // 4. Validar el nuevo horario, excluyendo la reserva actual de la comprobación
        List<ReservaPabellon> reservasSuperpuestas = reservaRepository.findOverlappingReservasExcluyendoActual(
                reserva.getPabellonId(), dto.getFechaHrInicio(), dto.getFechaHrTermino(), reservaId);

        if (!reservasSuperpuestas.isEmpty()) {
            throw new RuntimeException("El nuevo horario seleccionado entra en conflicto con otra reserva existente.");
        }

        // 5. Actualizar los campos de la entidad
        reserva.setFechaHrInicio(dto.getFechaHrInicio());
        reserva.setFechaHrTermino(dto.getFechaHrTermino());
        reserva.setMotivo(dto.getMotivo());
        reserva.setUrgencia(dto.getUrgencia());
        reserva.setComentario(dto.getComentario());

        // Opcional: Si la modificación requiere nueva aprobación, cambiar estado a "Pendiente"
        // EstadoSolicitud estadoPendiente = estadoRepository.findById(1).orElseThrow(...);
        // reserva.setEstadoId(estadoPendiente.getId());

        ReservaPabellon reservaModificada = reservaRepository.save(reserva);

        // 6. Devolver la respuesta enriquecida
        return toResponseDTO(reservaModificada); // toResponseDTO ya busca los datos del pabellón y usuario
    }

    @Transactional
    public ReservaPabellonResponseDTO cancelarReserva(Integer reservaId, Integer solicitanteId) {
        // 1. Encontrar la reserva
        ReservaPabellon reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("No se encontró la reserva con ID: " + reservaId));

        // 2. Verificar permisos: solo el usuario que la creó puede cancelarla
        if (!reserva.getUsuarioId().equals(solicitanteId)) {
            throw new RuntimeException("No tiene permisos para cancelar esta reserva.");
        }

        // 3. Verificar estado: no se puede cancelar si ya está finalizada o rechazada
        Integer estadoActualId = reserva.getEstadoId();
        if (estadoActualId == 3 || estadoActualId == 4) { // Asumiendo 3: Rechazada, 4: Finalizada
            throw new RuntimeException("Esta reserva ya no puede ser cancelada.");
        }

        // 4. Cambiar el estado a "Cancelada" (asumiendo que el ID 5 es "Cancelada")
        EstadoSolicitud estadoCancelado = estadoRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("El estado 'Cancelada' no se encuentra en la base de datos."));
        reserva.setEstadoId(estadoCancelado.getId());

        ReservaPabellon reservaCancelada = reservaRepository.save(reserva);

        // Reutilizamos el método que ya tienes para construir la respuesta
        UsuarioDTO usuario = usuarioClient.obtenerUsuarioPorId(reservaCancelada.getUsuarioId());
        PabellonDTO pabellon = pabellonClient.obtenerPabellonPorId(reservaCancelada.getPabellonId());

        return toResponseDTO(reservaCancelada, usuario, pabellon);
    }

    @Transactional
    public ReservaPabellonResponseDTO crearReserva(ReservaPabellonRequestDTO dto, Integer userId) {
        // Añadimos un bloque try-catch para capturar cualquier excepción inesperada
        try {
            System.out.println("--- INICIO DE CREAR RESERVA ---");
            System.out.println("Recibido DTO: " + dto.toString());
            System.out.println("Recibido userId: " + userId);

            // 1.1: Validar pabellón
            System.out.println("Paso 1.1: Validando pabellón con ID: " + dto.getPabellonId());
            PabellonDTO pabellon = pabellonClient.obtenerPabellonPorId(dto.getPabellonId());
            if (pabellon == null) {
                throw new RuntimeException("Reserva rechazada: El pabellón con ID " + dto.getPabellonId() + " no existe.");
            }
            if (!"Disponible".equalsIgnoreCase(pabellon.getEstado().getNombre())) {
                throw new RuntimeException("Reserva rechazada: El pabellón no se encuentra 'Disponible'.");
            }
            System.out.println("Pabellón validado con éxito.");

            // 1.2: Validar usuario
            System.out.println("Paso 1.2: Validando usuario con ID: " + userId);
            UsuarioDTO usuario = usuarioClient.obtenerUsuarioPorId(userId);
            if (usuario == null) {
                throw new RuntimeException("Reserva rechazada: El usuario con ID " + userId + " no existe.");
            }
            System.out.println("Usuario validado con éxito: " + usuario.getUsername());

            // 1.3: Validar superposición de horarios
            System.out.println("Paso 1.3: Validando superposición de horarios...");
            List<ReservaPabellon> reservasSuperpuestas = reservaRepository.findOverlappingReservas(
                    dto.getPabellonId(), dto.getFechaHrInicio(), dto.getFechaHrTermino());

            if (!reservasSuperpuestas.isEmpty()) {
                throw new RuntimeException("Reserva rechazada: Ya existe una reserva en el horario solicitado.");
            }
            System.out.println("Validación de horarios completada con éxito. No hay superposición.");

            // --- PASO 2: Crear y guardar la entidad ---
            ReservaPabellon reserva = new ReservaPabellon();
            // ... (el resto de los setters de la entidad)
            reserva.setFechaSolicitud(LocalDateTime.now());
            reserva.setFechaConfirmacion(null);
            reserva.setFechaHrInicio(dto.getFechaHrInicio());
            reserva.setFechaHrTermino(dto.getFechaHrTermino());
            reserva.setMotivo(dto.getMotivo());
            reserva.setUrgencia(dto.getUrgencia());
            reserva.setComentario(dto.getComentario());
            reserva.setPabellonId(pabellon.getId());
            reserva.setUsuarioId(usuario.getIdUsuario());

            EstadoSolicitud estado = estadoRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Estado inicial 'Pendiente' (ID 1) no encontrado."));
            reserva.setEstadoId(estado.getId());

            System.out.println("Paso 2: Objeto ReservaPabellon creado. A punto de guardar...");
            System.out.println("Entidad a guardar: " + reserva.toString());

            // El guardado real
            ReservaPabellon reservaGuardada = reservaRepository.save(reserva);

            System.out.println("¡ÉXITO! Repositorio 'save' ejecutado. Nuevo ID de reserva: " + reservaGuardada.getId());

            // --- PASO 3: Construir la respuesta ---
            System.out.println("Paso 3: Construyendo DTO de respuesta...");
            ReservaPabellonResponseDTO responseDTO = toResponseDTO(reservaGuardada, usuario, pabellon);
            System.out.println("--- FIN DE CREAR RESERVA (EXITOSO) ---");

            return responseDTO;

        } catch (Exception e) {
            // Si algo falla, lo veremos en la consola
            System.err.println("!!! ERROR DENTRO DE crearReserva. INICIANDO ROLLBACK !!!");
            System.err.println("Causa del error: " + e.getClass().getName());
            System.err.println("Mensaje del error: " + e.getMessage());
            e.printStackTrace(); // Imprime la traza completa del error en la consola

            // Volvemos a lanzar la excepción para asegurar que la transacción se revierta
            throw new RuntimeException("Error al crear la reserva: " + e.getMessage(), e);
        }
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

    /**
     * Devuelve una lista de todas las reservas de UN usuario específico.
     * @param usuarioId El ID del usuario cuyas reservas se quieren buscar.
     * @return Lista de DTOs de las reservas encontradas.
     */
    @Transactional(readOnly = true)
    public List<ReservaPabellonResponseDTO> listarReservasPorUsuario(Integer usuarioId) {
        // 1. Llama al nuevo método del repositorio para obtener solo las reservas de este usuario.
        List<ReservaPabellon> reservas = reservaRepository.findByUsuarioId(usuarioId);
        if (reservas.isEmpty()) {
            return List.of();
        }

        // 2. Reutilizamos la misma optimización N+1 que ya teníamos.
        // Recopilamos los IDs de pabellones de las reservas encontradas.
        List<Integer> pabellonIds = reservas.stream().map(ReservaPabellon::getPabellonId).distinct().toList();

        // Obtenemos los datos del usuario (solo será uno) y los pabellones en llamadas masivas.
        UsuarioDTO usuario = usuarioClient.obtenerUsuarioPorId(usuarioId);
        Map<Integer, PabellonDTO> pabellonesMap = pabellonClient.obtenerPabellonesPorIds(pabellonIds).stream()
                .collect(Collectors.toMap(PabellonDTO::getId, Function.identity()));

        // 3. Construimos la respuesta.
        return reservas.stream().map(reserva -> {
            PabellonDTO pabellon = pabellonesMap.get(reserva.getPabellonId());
            // Usamos la versión de toResponseDTO que no necesita hacer más llamadas de red.
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