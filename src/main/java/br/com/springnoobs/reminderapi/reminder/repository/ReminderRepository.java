package br.com.springnoobs.reminderapi.reminder.repository;

import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Page<Reminder> findAllByOrderByRemindAtAsc(Pageable pageable);

    List<Reminder> findBySentFalseAndExecutedAtIsNullAndRemindAtAfter(Instant now);

    @Query("SELECT r FROM Reminder r JOIN FETCH r.user u JOIN FETCH u.contact WHERE r.id = :id")
    Optional<Reminder> findByIdWithAssociations(Long id);
}
