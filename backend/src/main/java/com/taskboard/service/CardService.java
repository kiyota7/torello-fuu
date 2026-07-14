package com.taskboard.service;

import com.taskboard.dto.CardResponse;
import com.taskboard.dto.UpdateCardRequest;
import com.taskboard.entity.Card;
import com.taskboard.entity.TaskList;
import com.taskboard.exception.CardNotFoundException;
import com.taskboard.repository.CardRepository;
import com.taskboard.repository.TaskListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final TaskListRepository taskListRepository;

    public CardService(CardRepository cardRepository, TaskListRepository taskListRepository) {
        this.cardRepository = cardRepository;
        this.taskListRepository = taskListRepository;
    }

    @Transactional
    public CardResponse updateCard(Long cardId, UpdateCardRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (request.text() == null || request.text().isBlank()) {
            throw new IllegalArgumentException("カードの内容を入力してください。");
        }
        if (request.listId() == null) {
            throw new IllegalArgumentException("移動先のリストを選択してください。");
        }

        TaskList list = taskListRepository.findById(request.listId())
                .orElseThrow(() -> new IllegalArgumentException("移動先のリストが見つかりません: " + request.listId()));

        card.setText(request.text());
        card.setPriority(request.priority());
        card.setDueDate(request.dueDate());
        card.setList(list);

        card = cardRepository.save(card);
        return CardResponse.from(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        cardRepository.delete(card);
    }
}
