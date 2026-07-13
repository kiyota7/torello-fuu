package com.taskboard.controller;

import com.taskboard.dto.TaskListResponse;
import com.taskboard.service.BoardQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoardController {

    private final BoardQueryService boardQueryService;

    public BoardController(BoardQueryService boardQueryService) {
        this.boardQueryService = boardQueryService;
    }

    @GetMapping("/api/users/{userId}/lists")
    public List<TaskListResponse> getLists(@PathVariable Long userId) {
        return boardQueryService.getListsForUser(userId);
    }
}
