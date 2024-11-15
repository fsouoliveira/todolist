package com.fsouoliveira.todolist.task;

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
        var idUser = request.getAttribute("idUser");
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found.");
        }
        taskModel.setIdUser((UUID) idUser);

        if (taskModel.getStartAt() == null || taskModel.getEndAT() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start and end dates are required.");
        }

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAT())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The start / termination date must be in the future.");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAT())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The start date must precede the end date.");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public ResponseEntity<?> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        if (idUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found.");
        }

        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public TaskModel update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {

        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);
        taskModel.setId(id);
        return this.taskRepository.save(taskModel);
    }
}
