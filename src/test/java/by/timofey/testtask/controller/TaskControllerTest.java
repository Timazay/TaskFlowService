package by.timofey.testtask.controller;

import by.timofey.testtask.dto.AssignUserToTaskEvent;
import by.timofey.testtask.dto.request.AssignUserRequest;
import by.timofey.testtask.dto.request.ChangeTaskStatusRequest;
import by.timofey.testtask.dto.request.CreateTaskRequest;
import by.timofey.testtask.dto.response.CreateTaskResponse;
import by.timofey.testtask.dto.response.FindAllTasksResponse;
import by.timofey.testtask.dto.response.FindTaskResponse;
import by.timofey.testtask.dto.UserDto;
import by.timofey.testtask.entity.enums.TaskStatus;
import by.timofey.testtask.exception.NotFoundException;
import by.timofey.testtask.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getTask_WhenTaskExists_ShouldReturnTask() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto(userId, "John Doe", "john@example.com");
        FindTaskResponse response = new FindTaskResponse(
                "Test Task",
                "Test Description",
                TaskStatus.IN_PROGRESS,
                userDto
        );

        when(taskService.findByTaskId(taskId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.name").value("John Doe"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));

        verify(taskService, times(1)).findByTaskId(taskId);
    }

    @Test
    void getTask_WhenThereIsNoTask_ShouldReturnNotFound() throws Exception {
        UUID taskId = UUID.randomUUID();

        doThrow(new NotFoundException("Task not found"))
                .when(taskService).findByTaskId(taskId);

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.uri").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").value("Task not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, times(1)).findByTaskId(taskId);
    }

    @Test
    void getTasks_WhenSingleTaskExists_ShouldReturnListWithOneTask() throws Exception {
        int page = 0;
        int size = 10;

        UUID taskId = UUID.randomUUID();
        FindAllTasksResponse task = new FindAllTasksResponse(taskId, "Single Task", "Single Description");

        when(taskService.findAllTasks(page, size)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()))
                .andExpect(jsonPath("$[0].name").value("Single Task"))
                .andExpect(jsonPath("$[0].description").value("Single Description"));

        verify(taskService, times(1)).findAllTasks(page, size);
    }

    @Test
    void createTask_WhenValidRequest_ShouldReturnCreatedStatus() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "Smth",
                "Smth"
        );
        UUID taskId = UUID.randomUUID();

        when(taskService.createTask(request)).thenReturn(new CreateTaskResponse(taskId));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));

        verify(taskService, times(1)).createTask(any(CreateTaskRequest.class));
    }

    @Test
    void createTask_WhenNameIsNull_ShouldReturnBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                null,
                "Description without name"
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.uri").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).createTask(any(CreateTaskRequest.class));
    }

    @Test
    void createTask_WhenDescriptionIsNull_ShouldReturnBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "name without description",
                null
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.uri").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).createTask(any(CreateTaskRequest.class));
    }

    @Test
    void createTask_WhenNameContainsLessThenThreeLetters_ShouldReturnBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "na",
                "smth"
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.uri").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).createTask(any(CreateTaskRequest.class));
    }

    @Test
    void changeTaskStatus_WhenValidRequest_ShouldReturnOk() throws Exception {
        UUID taskId = UUID.randomUUID();

        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest(TaskStatus.IN_PROGRESS);

        doNothing().when(taskService).changeTaskStatus(taskId, request);

        mockMvc.perform(put("/api/v1/tasks/{taskId}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(taskService, times(1)).changeTaskStatus(taskId, request);
    }

    @Test
    void changeTaskStatus_WhenTaskNotFound_ShouldReturnNotFound() throws Exception {
        UUID taskId = UUID.randomUUID();
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest(TaskStatus.IN_PROGRESS);

        doThrow(new NotFoundException("Task not found"))
                .when(taskService).changeTaskStatus(taskId, request);

        mockMvc.perform(put("/api/v1/tasks/{taskId}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.uri").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").value("Task not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, times(1)).changeTaskStatus(taskId, request);
    }

    @Test
    void changeTaskStatus_WhenStatusIsNull_ShouldReturnBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest(null);

        mockMvc.perform(put("/api/v1/tasks/{taskId}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.uri").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).changeTaskStatus(any(UUID.class), any(ChangeTaskStatusRequest.class));
    }

    @Test
    void assignUserToTaskPatch_WhenValidRequest_ShouldReturnOk() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AssignUserRequest assignUserRequest = new AssignUserRequest(userId);

        doNothing().when(taskService).assignUserToTask(any(AssignUserToTaskEvent.class));

        mockMvc.perform(put("/api/v1/tasks/{taskId}/assignee", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignUserRequest)))
                .andExpect(status().isOk());

        verify(taskService, times(1)).assignUserToTask(argThat(event ->
                event.taskId().equals(taskId) &&
                        event.userId().equals(userId)
        ));
    }

    @Test
    void assignUserToTaskPatch_WhenUserIdIsNull_ShouldReturnBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        AssignUserRequest invalidRequest = new AssignUserRequest(null);

        mockMvc.perform(put("/api/v1/tasks/{taskId}/assignee", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).assignUserToTask(any());
    }

    @Test
    void assignUserToTaskPatch_WhenTaskNotFound_ShouldReturnNotFound() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AssignUserRequest assignUserRequest = new AssignUserRequest(userId);

        doThrow(new NotFoundException("Task not found with id: " + taskId))
                .when(taskService).assignUserToTask(any(AssignUserToTaskEvent.class));

        mockMvc.perform(put("/api/v1/tasks/{taskId}/assignee", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Task not found with id: " + taskId));

        verify(taskService, times(1)).assignUserToTask(any());
    }
}
