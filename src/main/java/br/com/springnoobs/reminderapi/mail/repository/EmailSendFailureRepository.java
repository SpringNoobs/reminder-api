package br.com.springnoobs.reminderapi.mail.repository;

import br.com.springnoobs.reminderapi.mail.entity.EmailSendFailure;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendFailureRepository extends JpaRepository<EmailSendFailure, Long> {

    List<EmailSendFailure> findTop20ByOrderByFailedAtAsc();
}
