package com.taskboard.dto;

import com.taskboard.entity.Card;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String text,
        String priority,
        LocalDate dueDate,
        Integer position
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getText(),
                card.getPriority(),
                card.getDueDate(),
                card.getPosition()
        );
    }
}
