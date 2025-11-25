package br.com.springnoobs.reminderapi.user.dto.response;

public record UserResponseDTO(Long id, String firstName, String lastName, ContactResponseDTO contact) {}
