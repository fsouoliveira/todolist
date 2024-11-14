package com.fsouoliveira.todolist.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel) {

        if (taskModel.getIdUser() == null) {
            taskModel.setIdUser(UUID.randomUUID());
        }

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAT())) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The start / termination date must be in the future.");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAT())) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The start date must precede the end date.");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }
}
