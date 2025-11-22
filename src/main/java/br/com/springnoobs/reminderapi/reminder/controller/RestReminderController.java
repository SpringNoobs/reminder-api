package br.com.springnoobs.reminderapi.reminder.controller;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.service.ReminderService;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reminders")
public class RestReminderController {
    private final ReminderService reminderService;
    public RestReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // OBS: acho que seria melhor retornar o id de cada reminder
    @GetMapping
    public ResponseEntity<List<ReminderResponseDTO>>  findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reminderService.findAll(pageable));
    }

// // ABAIXO um exemplo de findAll com Page
// // O problema é que o 666L teria que representar o total geral de elementos no banco de dados, e não tenho essa informação vinda do service
// // Então se for para  retornar um Page, o ideal seria mudar o código no service para retornar um Page diretamente
//@GetMapping
//public ResponseEntity<Page<ReminderResponseDTO>> findAll(
//        @RequestParam(defaultValue = "0") int page,
//        @RequestParam(defaultValue = "10") int size) {
//
//    Pageable pageable = PageRequest.of(page, size);
//    List<ReminderResponseDTO> reminderList = reminderService.findAll(pageable);
//    Page<ReminderResponseDTO> reminderPage = new PageImpl<>(
//            reminderList,
//            pageable,
//            666L
//    );
//
//    return ResponseEntity.ok(reminderPage);
//}

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


