package com.taskboard.dto;

import java.time.LocalDate;

public record UpdateCardRequest(String text, String priority, LocalDate dueDate, Long listId) {
}
