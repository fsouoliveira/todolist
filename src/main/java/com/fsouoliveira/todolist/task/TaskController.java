package com.fsouoliveira.todolist.task;

import com.fsouoliveira.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        try {
            UUID idUser = (UUID) request.getAttribute("idUser");
            if (idUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found.");
            }
            taskModel.setIdUser(idUser);

            if (taskModel.getStartAt() == null || taskModel.getEndAT() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start and end dates are required.");
            }

            var currentDate = LocalDateTime.now();
            if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAT())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dates must be in the future.");
            }

            if (taskModel.getStartAt().isAfter(taskModel.getEndAT())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The start date must precede the end date.");
            }

            var task = this.taskRepository.save(taskModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user ID format.");
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> list(HttpServletRequest request) {
        try {
            UUID idUser = (UUID) request.getAttribute("idUser");
            if (idUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found.");
            }

            var tasks = this.taskRepository.findByIdUser(idUser);
            return ResponseEntity.ok(tasks);
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user ID format.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        try {
            UUID idUser = (UUID) request.getAttribute("idUser");
            if (idUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found.");
            }

            var taskOptional = this.taskRepository.findById(id);
            if (taskOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found.");
            }

            var task = taskOptional.get();
            if (!task.getIdUser().equals(idUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User without permission to change this task.");
            }

            Utils.copyNonNullProperties(taskModel, task);
            var taskUpdate = this.taskRepository.save(task);
            return ResponseEntity.ok(taskUpdate);
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user ID format.");
        }
    }
}

