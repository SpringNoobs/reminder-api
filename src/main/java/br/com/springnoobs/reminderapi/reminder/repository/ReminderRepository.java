package br.com.springnoobs.reminderapi.reminder.repository;

import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Page<Reminder> findAllByOrderByRemindAtAsc(Pag pageable);

    List<Reminder> findBySentFalseAndExecutedAtIsNullAndRemindAtAfter(Instant now);
}
