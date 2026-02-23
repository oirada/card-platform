package com.oscar.cardplatform.web.controller;

import com.oscar.cardplatform.domain.entity.Card;
import com.oscar.cardplatform.service.CardService;
import com.oscar.cardplatform.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Tarjetas", description = "Operaciones de gestión de tarjetas")
public class CardController {

    private final CardService cardService;
    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @PostMapping
    @Operation(summary = "Crear tarjeta", description = "Crea una nueva tarjeta en el sistema. Retorna número de validación (1-100), identificador único y PAN enmascarado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarjeta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    public ResponseEntity<CreateCardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        try {
            Card card = cardService.createCard(request.pan(), request.titular(), request.cedula(), request.tipo(), request.telefono());
            int numeroValidacion = card.getNumeroValidacion();
            CreateCardResponse response = new CreateCardResponse("00", "Éxito", numeroValidacion, card.getMaskedPan(), card.getIdentificador());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid create card request: {}", iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateCardResponse("01", "Operación inválida", null, null, null));
        } catch (Exception e) {
            logger.error("Unexpected error creating card", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateCardResponse("01", "Operación inválida", null, null, null));
        }
    }

    @PostMapping("/enrol")
    @Operation(summary = "Enrolar tarjeta", description = "Activa una tarjeta si el número de validación es correcto. Cambia estado a ENROLADA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarjeta enrolada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Operación inválida (tarjeta no existe o validación fallida)")
    })
    public ResponseEntity<EnrolCardResponse> enrol(@Valid @RequestBody EnrolCardRequest request) {
        try {
            Card card = cardService.enrolCard(request.identificador(), request.numeroValidacion());
            EnrolCardResponse resp = new EnrolCardResponse("00", "Éxito", card.getMaskedPan());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException iae) {
            logger.warn("Enrol attempt failed (hide detail): {}", iae.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new EnrolCardResponse("01", "Operación inválida", null));
        } catch (Exception e) {
            logger.error("Unexpected error during enrol", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new EnrolCardResponse("01", "Operación inválida", null));
        }
    }

    @GetMapping("/{identificador}")
    @Operation(summary = "Consultar tarjeta", description = "Obtiene los detalles de una tarjeta usando su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos de la tarjeta"),
            @ApiResponse(responseCode = "404", description = "Operación inválida (tarjeta no encontrada)")
    })
    public ResponseEntity<CardDetailResponse> getByIdentificador(@PathVariable String identificador) {
        try {
            Card card = cardService.getCardByIdentificador(identificador);
            return ResponseEntity.ok(new CardDetailResponse(card.getMaskedPan(), card.getTitular(), card.getCedula(), card.getTelefono(), card.getStatus().name()));
        } catch (IllegalArgumentException iae) {
            logger.warn("Get card by id failed (hide detail): {}", iae.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{identificador}")
    @Operation(summary = "Eliminar tarjeta", description = "Realiza borrado lógico de una tarjeta (marca estado como INACTIVE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarjeta eliminada"),
            @ApiResponse(responseCode = "404", description = "Operación inválida (tarjeta no encontrada)")
    })
    public ResponseEntity<DeleteCardResponse> delete(@PathVariable String identificador) {
        try {
            cardService.deleteCardLogical(identificador);
            return ResponseEntity.ok(new DeleteCardResponse("00", "Se ha eliminado la tarjeta"));
        } catch (IllegalArgumentException iae) {
            logger.warn("Delete card failed (hide detail): {}", iae.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DeleteCardResponse("01", "Operación inválida"));
        } catch (Exception e) {
            logger.error("Unexpected error deleting card", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DeleteCardResponse("01", "No se ha eliminado la tarjeta"));
        }
    }
}
