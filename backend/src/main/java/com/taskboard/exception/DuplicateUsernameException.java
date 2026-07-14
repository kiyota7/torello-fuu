package com.taskboard.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("そのユーザー名は既に使用されています: " + username);
    }
}
