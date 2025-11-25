package br.com.springnoobs.reminderapi.user.repository;

import br.com.springnoobs.reminderapi.user.entity.Contact;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    boolean existsByEmail(String email);

    Optional<Contact> findByEmail(String email);
}
