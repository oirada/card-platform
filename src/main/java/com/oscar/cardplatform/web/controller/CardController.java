package com.oscar.cardplatform.web.controller;

import com.oscar.cardplatform.domain.entity.Card;
import com.oscar.cardplatform.service.CardService;
import com.oscar.cardplatform.web.dto.*;
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
public class CardController {

    private final CardService cardService;
    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @PostMapping
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
