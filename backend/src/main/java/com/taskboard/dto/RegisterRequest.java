package com.taskboard.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "ユーザー名を入力してください。") String username,
        @NotBlank(message = "パスワードを入力してください。") String password
) {
}
