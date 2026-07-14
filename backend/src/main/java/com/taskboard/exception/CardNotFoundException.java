package com.taskboard.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long cardId) {
        super("カードが見つかりません: " + cardId);
    }
}
