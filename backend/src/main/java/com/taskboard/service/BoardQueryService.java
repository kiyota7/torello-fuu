package com.taskboard.service;

import com.taskboard.dto.CardResponse;
import com.taskboard.dto.TaskListResponse;
import com.taskboard.entity.TaskList;
import com.taskboard.repository.CardRepository;
import com.taskboard.repository.TaskListRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BoardQueryService {

    private final TaskListRepository taskListRepository;
    private final CardRepository cardRepository;

    public BoardQueryService(TaskListRepository taskListRepository, CardRepository cardRepository) {
        this.taskListRepository = taskListRepository;
        this.cardRepository = cardRepository;
    }

    public List<TaskListResponse> getListsForUser(Long userId) {
        List<TaskList> lists = taskListRepository.findByUserIdOrderByPosition(userId);
        return lists.stream()
                .map(list -> {
                    List<CardResponse> cards = cardRepository.findByListIdOrderByPosition(list.getId())
                            .stream()
                            .map(CardResponse::from)
                            .toList();
                    return TaskListResponse.from(list, cards);
                })
                .toList();
    }
}
