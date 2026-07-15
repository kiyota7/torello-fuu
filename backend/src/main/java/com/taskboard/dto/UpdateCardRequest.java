package com.taskboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record UpdateCardRequest(
        @NotBlank(message = "カードの内容を入力してください。") String text,
        @NotBlank(message = "優先度を選択してください。")
        @Pattern(regexp = "高|中|低", message = "優先度は「高」「中」「低」のいずれかを指定してください。") String priority,
        LocalDate dueDate,
        @NotNull(message = "移動先のリストを選択してください。") Long listId,
        @NotNull(message = "ユーザーIDを指定してください。") Long userId
) {
}
