package com.oscar.cardplatform.web.controller;

import com.oscar.cardplatform.domain.entity.Card;
import com.oscar.cardplatform.service.CardService;
import com.oscar.cardplatform.web.dto.CardResponse;
import com.oscar.cardplatform.web.dto.CreateCardRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        Card card = cardService.createCard(request.pan());
        return ResponseEntity.ok(toResponse(card));
    }

    @GetMapping
    public ResponseEntity<CardResponse> getByPan(@RequestParam String pan) {
        Card card = cardService.getCardByPan(pan);
        return ResponseEntity.ok(toResponse(card));
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(card.getId(), card.getMaskedPan(), card.getStatus().name());
    }
}
