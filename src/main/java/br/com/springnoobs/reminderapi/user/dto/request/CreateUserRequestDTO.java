package br.com.springnoobs.reminderapi.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequestDTO(
        @NotBlank(message = "First name cannot be blank") String firstName,
        @NotBlank(message = "Last name cannot be blank") String lastName,
        ContactRequestDTO contactRequestDTO) {}
