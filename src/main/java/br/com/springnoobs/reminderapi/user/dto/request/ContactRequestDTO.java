package br.com.springnoobs.reminderapi.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequestDTO(
        @NotBlank(message = "Email cannot be blank") @Email String email,
        @NotBlank(message = "Phone number cannot be blank") String phoneNumber) {}
