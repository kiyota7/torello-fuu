package com.taskboard.controller;

import com.taskboard.dto.CardResponse;
import com.taskboard.dto.UpdateCardRequest;
import com.taskboard.service.CardService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PutMapping("/api/cards/{cardId}")
    public CardResponse updateCard(@PathVariable Long cardId, @RequestBody UpdateCardRequest request) {
        return cardService.updateCard(cardId, request);
    }
}
