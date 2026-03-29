package by.timofey.testtask.service;

import by.timofey.testtask.config.KafkaPropertyConfig;
import by.timofey.testtask.config.properties.TopicProperty;
import by.timofey.testtask.dto.AssignUserToTaskEvent;
import by.timofey.testtask.dto.request.ChangeTaskStatusRequest;
import by.timofey.testtask.dto.request.CreateTaskRequest;
import by.timofey.testtask.dto.response.CreateTaskResponse;
import by.timofey.testtask.dto.response.FindAllTasksResponse;
import by.timofey.testtask.dto.response.FindTaskResponse;
import by.timofey.testtask.entity.Task;
import by.timofey.testtask.entity.User;
import by.timofey.testtask.entity.enums.TaskStatus;
import by.timofey.testtask.exception.ConflictException;
import by.timofey.testtask.exception.NotFoundException;
import by.timofey.testtask.repository.TaskRepository;
import by.timofey.testtask.repository.UserRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaPropertyConfig kafkaConfig;

    @Mock
    private UserRepository userRepository;

    @Test
    void findByTaskId_WhenTaskAndUserExist_ShouldReturnResponse() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("John Doe")
                .email("john@example.com")
                .build();
        Task task = Task.builder()
                .id(taskId)
                .name("Test Task")
                .description("Test Description")
                .status(TaskStatus.IN_PROGRESS)
                .user(user)
                .build();

        when(taskRepository.findFullTaskById(taskId)).thenReturn(Optional.of(task));

        FindTaskResponse response = taskService.findByTaskId(taskId);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Test Task");
        assertThat(response.description()).isEqualTo("Test Description");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.user()).isNotNull();
        assertThat(response.user().id()).isEqualTo(userId);
        assertThat(response.user().name()).isEqualTo("John Doe");
        assertThat(response.user().email()).isEqualTo("john@example.com");

        verify(taskRepository, times(1)).findFullTaskById(taskId);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void findByTaskId_WhenTaskExistsWithoutUser_ShouldReturnResponseWithNullUser() {
        UUID taskId = UUID.randomUUID();
        Task task = Task.builder()
                .id(taskId)
                .name("Task Without User")
                .description("No user assigned")
                .status(TaskStatus.NEW)
                .user(null)
                .build();

        when(taskRepository.findFullTaskById(taskId)).thenReturn(Optional.of(task));

        FindTaskResponse response = taskService.findByTaskId(taskId);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Task Without User");
        assertThat(response.description()).isEqualTo("No user assigned");
        assertThat(response.status()).isEqualTo(TaskStatus.NEW);
        assertThat(response.user()).isNull();

        verify(taskRepository, times(1)).findFullTaskById(taskId);
    }

    @Test
    void findByTaskId_WhenTaskNotExists_ShouldThrowNotFoundException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findFullTaskById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findByTaskId(taskId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");

        verify(taskRepository, times(1)).findFullTaskById(taskId);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void findAllTasks_WhenTasksExists_ShouldReturnListOfResponses() {
        int page = 0;
        int size = 10;

        UUID taskId1 = UUID.randomUUID();
        Task task1 = Task.builder()
                .id(taskId1)
                .name("Task 1")
                .description("Description 1")
                .build();
        UUID taskId2 = UUID.randomUUID();
        Task task2 = Task.builder()
                .id(taskId2)
                .name("Task 2")
                .description("Description 2")
                .build();
        UUID taskId3 = UUID.randomUUID();
        Task task3 = Task.builder()
                .id(taskId3)
                .name("Task 3")
                .description("Description 3")
                .build();

        List<Task> tasks = List.of(task1, task2, task3);
        Page<Task> taskPage = new PageImpl<>(tasks, PageRequest.of(page, size), tasks.size());

        when(taskRepository.findAll(any(Pageable.class))).thenReturn(taskPage);

        List<FindAllTasksResponse> responses = taskService.findAllTasks(page, size);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(3);

        assertThat(responses.get(0))
                .extracting(
                        FindAllTasksResponse::taskId,
                        FindAllTasksResponse::name,
                        FindAllTasksResponse::description
                )
                .containsExactly(taskId1, "Task 1", "Description 1");

        assertThat(responses.get(1))
                .extracting(
                        FindAllTasksResponse::taskId,
                        FindAllTasksResponse::name,
                        FindAllTasksResponse::description
                )
                .containsExactly(taskId2, "Task 2", "Description 2");

        assertThat(responses.get(2))
                .extracting(
                        FindAllTasksResponse::taskId,
                        FindAllTasksResponse::name,
                        FindAllTasksResponse::description
                )
                .containsExactly(taskId3, "Task 3", "Description 3");

        verify(taskRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void findAllTasks_WhenThereIsNoTasks_ShouldReturnEmptyList() {
        int page = 0;
        int size = 10;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        List<Task> emptyTasks = List.of();
        Page<Task> emptyPage = new PageImpl<>(emptyTasks, pageable, 0);

        when(taskRepository.findAll(pageable)).thenReturn(emptyPage);

        List<FindAllTasksResponse> responses = taskService.findAllTasks(page, size);

        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(taskRepository, times(1)).findAll(pageable);
    }

    @Test
    void createTask_WhenValidRequest_ShouldSaveAndSendProducerRecordWithCorrectTopic() {
        UUID taskId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("Test Task", "Test Description");

        Task savedTask = Task.builder()
                .id(taskId)
                .name(request.name())
                .description(request.description())
                .status(TaskStatus.NEW)
                .build();

        TopicProperty topicProperty = new TopicProperty("", "task-create-event");

        when(kafkaConfig.getTopics()).thenReturn(topicProperty);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        CreateTaskResponse response = taskService.createTask(request);

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));

        assertThat(response).isNotNull();
        assertThat(response.taskId()).isEqualTo(taskId);
    }

    @Test
    void changeTaskStatus_WhenTaskExists_ShouldUpdateStatus() {
        UUID taskId = UUID.randomUUID();
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest(TaskStatus.IN_PROGRESS);

        Task existingTask = Task.builder()
                .id(taskId)
                .name("Test Task")
                .description("Test Description")
                .status(TaskStatus.NEW)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        taskService.changeTaskStatus(taskId, request);

        assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    void changeTaskStatus_WhenTaskNotExists_ShouldThrowNotFoundExceptionBDD() {
        UUID nonExistentTaskId = UUID.randomUUID();
        ChangeTaskStatusRequest request = new ChangeTaskStatusRequest(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.changeTaskStatus(nonExistentTaskId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");

        verify(taskRepository, times(1)).findById(nonExistentTaskId);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void assignUserToTask_WhenTaskNotFound_ShouldThrowNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AssignUserToTaskEvent event = new AssignUserToTaskEvent(taskId, userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignUserToTask(event))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");

        verify(taskRepository).findById(taskId);
        verify(userRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }


    @Test
    void assignUserToTask_WhenUserNotFound_ShouldThrowNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Task task = Task.builder()
                .id(taskId)
                .name("Test Task")
                .description("Test Description")
                .status(TaskStatus.NEW)
                .build();

        AssignUserToTaskEvent event = new AssignUserToTaskEvent(taskId, userId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignUserToTask(event))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");

        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(taskRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void assignUserToTask_WhenTaskAndUserExist_ShouldAssignUserAndSendKafkaMessage() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AssignUserToTaskEvent event = new AssignUserToTaskEvent(taskId, userId);

        Task task = Task.builder()
                .id(taskId)
                .name("Test Task")
                .description("Test Description")
                .status(TaskStatus.NEW)
                .build();

        User user = User.builder()
                .id(userId)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(kafkaConfig.getTopics()).thenReturn(new TopicProperty("task-assign-user", ""));

        taskService.assignUserToTask(event);

        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(taskRepository).save(argThat(savedTask ->
                savedTask.getUser() != null &&
                        savedTask.getUser().getId().equals(userId) &&
                        savedTask.getId().equals(taskId)
        ));
        verify(kafkaTemplate).send(
                eq("task-assign-user"),
                eq(event)
        );
        verify(taskRepository, times(1)).save(any());
        verify(kafkaTemplate, times(1)).send(anyString(), any());
    }

    @Test
    void assignUserToTask_WhenUserAlreadyAssigned_ShouldThrowConflictException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AssignUserToTaskEvent event = new AssignUserToTaskEvent(taskId, userId);

        Task task = Task.builder()
                .id(taskId)
                .name("Test Task")
                .description("Test Description")
                .status(TaskStatus.NEW)
                .build();

        User user = User.builder()
                .id(userId)
                .name("John Doe")
                .email("john@example.com")
                .build();
        task.setUser(user);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> taskService.assignUserToTask(event))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Task is already assigned to this user");

        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(taskRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }
}
