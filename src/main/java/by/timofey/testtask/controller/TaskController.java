package by.timofey.testtask.controller;

import by.timofey.testtask.dto.AssignUserToTaskEvent;
import by.timofey.testtask.dto.request.AssignUserRequest;
import by.timofey.testtask.dto.request.ChangeTaskStatusRequest;
import by.timofey.testtask.dto.CreateTaskEvent;
import by.timofey.testtask.dto.response.CreateTaskResponse;
import by.timofey.testtask.dto.response.FindAllTasksResponse;
import by.timofey.testtask.dto.response.FindTaskResponse;
import by.timofey.testtask.exception.ErrorResponseDto;
import by.timofey.testtask.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get task by ID", description = "Returns a single task by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FindTaskResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{taskId}")
    public FindTaskResponse getTask(@PathVariable UUID taskId) {
        return taskService.findByTaskId(taskId);
    }

    @Operation(summary = "Get all tasks", description = "Returns a list of all tasks with pagination")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FindAllTasksResponse.class)))
    })
    @GetMapping
    public List<FindAllTasksResponse> getTasks(@RequestParam int page, @RequestParam int size) {
        return taskService.findAllTasks(page, size);
    }

    @Operation(summary = "Create new task", description = "Creates a new task and returns the created task")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateTaskResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<CreateTaskResponse> createTask(@Valid @RequestBody CreateTaskEvent task) {
        CreateTaskResponse response = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Change task status", description = "Updates the status of an existing task")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task status changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task or user not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    ))
    })
    @PutMapping("/{taskId}/status")
    public ResponseEntity<Void> changeTaskStatus(@PathVariable UUID taskId,
                                                 @Valid @RequestBody ChangeTaskStatusRequest request) {
        taskService.changeTaskStatus(taskId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Assign user to task")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully assigned to task"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
            )),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task or user not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
            ))
    })
    @PutMapping("/{taskId}/assignee")
    public ResponseEntity<Void> assignUserToTaskPatch(
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignUserRequest request) {
        taskService.assignUserToTask(new AssignUserToTaskEvent(taskId, request.userId()));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
