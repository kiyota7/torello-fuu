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
                .filter(c -> c.getList().getUser().getId().equals(request.userId()))
                .orElseThrow(() -> new CardNotFoundException(cardId));

        TaskList list = taskListRepository.findById(request.listId())
                .filter(l -> l.getUser().getId().equals(request.userId()))
                .orElseThrow(() -> new IllegalArgumentException("移動先のリストが見つかりません: " + request.listId()));

        card.setText(request.text());
        card.setPriority(request.priority());
        card.setDueDate(request.dueDate());
        card.setList(list);

        card = cardRepository.save(card);
        return CardResponse.from(card);
    }

    @Transactional
    public void deleteCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .filter(c -> c.getList().getUser().getId().equals(userId))
                .orElseThrow(() -> new CardNotFoundException(cardId));
        cardRepository.delete(card);
    }
}
