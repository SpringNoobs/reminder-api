package br.com.springnoobs.reminderapi.user.service;

import br.com.springnoobs.reminderapi.user.dto.request.CreateUserRequestDTO;
import br.com.springnoobs.reminderapi.user.dto.response.UserResponseDTO;
import br.com.springnoobs.reminderapi.user.entity.Contact;
import br.com.springnoobs.reminderapi.user.entity.User;
import br.com.springnoobs.reminderapi.user.exception.EmailAlreadyExistsException;
import br.com.springnoobs.reminderapi.user.exception.EmailNotFoundException;
import br.com.springnoobs.reminderapi.user.exception.UserNotFoundException;
import br.com.springnoobs.reminderapi.user.mapper.UserMapper;
import br.com.springnoobs.reminderapi.user.repository.ContactRepository;
import br.com.springnoobs.reminderapi.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public UserService(UserRepository userRepository, ContactRepository contactRepository) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    @Transactional
    public UserResponseDTO create(CreateUserRequestDTO dto) {
        User user = createAndSaveUser(dto);

        return UserMapper.toResponse(user);
    }

    public UserResponseDTO findByEmail(String email) {
        Contact contact = contactRepository
                .findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("E-mail not found" + email));
        User user = contact.getUser();
        return UserMapper.toResponse(user);
    }

    public List<UserResponseDTO> findAll() {
        List<User> users = userRepository.findAll();
        return UserMapper.toResponseList(users);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found" + id));
        userRepository.delete(user);
    }

    public User createAndSaveUser(CreateUserRequestDTO request) {
        if (contactRepository.existsByEmail(request.contactRequestDTO().email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User savedUser = userRepository.save(user);

        Contact contact = new Contact();
        contact.setEmail(request.contactRequestDTO().email());
        contact.setPhoneNumber(request.contactRequestDTO().phoneNumber());
        contact.setUser(savedUser);

        contactRepository.save(contact);
        savedUser.setContact(contact);

        return savedUser;
    }
}
