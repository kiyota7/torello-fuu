package com.taskboard.dto;

import com.taskboard.entity.TaskList;
import java.util.List;

public record TaskListResponse(
        Long id,
        String title,
        Integer position,
        List<CardResponse> cards
) {
    public static TaskListResponse from(TaskList list, List<CardResponse> cards) {
        return new TaskListResponse(list.getId(), list.getTitle(), list.getPosition(), cards);
    }
}
