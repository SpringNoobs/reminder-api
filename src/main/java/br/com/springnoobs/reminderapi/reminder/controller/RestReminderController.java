package br.com.springnoobs.reminderapi.reminder.controller;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.service.ReminderService;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reminders")
public class RestReminderController {
    private final ReminderService reminderService;
    public RestReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }
@GetMapping
public ResponseEntity<Page<ReminderResponseDTO>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<ReminderResponseDTO> reminderPage = reminderService.findAll(pageable);
    return ResponseEntity.ok(reminderPage);
}

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reminderService.findById(id));
    }
    @PostMapping()
    public ResponseEntity<ReminderResponseDTO> create(@RequestBody @Valid CreateReminderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.create(dto));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponseDTO> update(@PathVariable Long id, @RequestBody @Valid UpdateReminderRequestDTO dto) {
        return ResponseEntity.ok(reminderService.update(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reminderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


