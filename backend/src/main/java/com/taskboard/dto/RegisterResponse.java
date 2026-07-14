package com.taskboard.dto;

import com.taskboard.entity.AppUser;

public record RegisterResponse(Long id, String username) {
    public static RegisterResponse from(AppUser user) {
        return new RegisterResponse(user.getId(), user.getUsername());
    }
}
