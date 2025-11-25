package br.com.springnoobs.reminderapi.user.mapper;

import br.com.springnoobs.reminderapi.user.dto.response.ContactResponseDTO;
import br.com.springnoobs.reminderapi.user.dto.response.UserResponseDTO;
import br.com.springnoobs.reminderapi.user.entity.User;
import java.util.List;

public class UserMapper {
    public static UserResponseDTO toResponse(User user) {
        var contact = user.getContact();

        if (contact == null) {
            return new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), null);
        }
        var contactDTO = new ContactResponseDTO(
                contact.getEmail() != null ? contact.getEmail() : null,
                contact.getPhoneNumber() != null ? contact.getPhoneNumber() : null);
        return new UserResponseDTO(user.getId(), user.getFirstName(), user.getLastName(), contactDTO);
    }

    public static List<UserResponseDTO> toResponseList(List<User> users) {
        return users.stream().map(UserMapper::toResponse).toList();
    }
}
