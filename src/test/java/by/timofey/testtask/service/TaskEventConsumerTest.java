package by.timofey.testtask.service;

import by.timofey.testtask.dto.CreateTaskEvent;
import by.timofey.testtask.entity.Task;
import by.timofey.testtask.entity.enums.TaskStatus;
import by.timofey.testtask.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskEventConsumerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Acknowledgment acknowledgment;

    @Captor
    private ArgumentCaptor<List<Task>> taskListCaptor;

    @InjectMocks
    private TaskEventConsumer taskEventConsumer;

    private UUID taskId1;
    private UUID taskId2;
    private List<CreateTaskEvent> events;
    private List<String> strTaskIds;

    @BeforeEach
    void setUp() {
        taskId1 = UUID.randomUUID();
        taskId2 = UUID.randomUUID();

       CreateTaskEvent event1 = new CreateTaskEvent("Task 1", "Description 1");
       CreateTaskEvent event2 = new CreateTaskEvent("Task 2", "Description 2");

        events = Arrays.asList(event1, event2);
        strTaskIds = Arrays.asList(taskId1.toString(), taskId2.toString());
    }

    @Test
    void consumeCreateTaskRequest_WhenTasksDoesNotExist_ShouldCreateNewTasksAndAcknowledge() {
        when(taskRepository.findAllById(anyList())).thenReturn(List.of());

        taskEventConsumer.consumeCreateTaskEvent(events, strTaskIds, acknowledgment);

        verify(taskRepository).findAllById(Arrays.asList(taskId1, taskId2));
        verify(taskRepository).saveAll(taskListCaptor.capture());
        verify(acknowledgment).acknowledge();

        List<Task> savedTasks = taskListCaptor.getValue();
        assertThat(savedTasks).hasSize(2);

        Task savedTask1 = savedTasks.getFirst();
        assertThat(savedTask1.getId()).isEqualTo(taskId1);
        assertThat(savedTask1.getName()).isEqualTo("Task 1");
        assertThat(savedTask1.getDescription()).isEqualTo("Description 1");
        assertThat(savedTask1.getStatus()).isEqualTo(TaskStatus.NEW);

        Task savedTask2 = savedTasks.get(1);
        assertThat(savedTask2.getId()).isEqualTo(taskId2);
        assertThat(savedTask2.getName()).isEqualTo("Task 2");
        assertThat(savedTask2.getDescription()).isEqualTo("Description 2");
        assertThat(savedTask2.getStatus()).isEqualTo(TaskStatus.NEW);

        verify(taskRepository, times(1)).saveAll(anyList());
    }

    @Test
    void consumeCreateTaskRequest_WhenTasksAlreadyExists_ShouldSkipSavingAndAcknowledge() {
        Task existingTask = Task.builder()
                .id(taskId1)
                .name("Task 1")
                .description("Description 1")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findAllById(Arrays.asList(taskId1, taskId2)))
                .thenReturn(List.of(existingTask));

        taskEventConsumer.consumeCreateTaskEvent(events, strTaskIds, acknowledgment);

        verify(taskRepository).findAllById(Arrays.asList(taskId1, taskId2));
        verify(taskRepository).saveAll(taskListCaptor.capture());
        verify(acknowledgment).acknowledge();

        List<Task> savedTasks = taskListCaptor.getValue();
        assertThat(savedTasks).hasSize(1);

        Task savedTask = savedTasks.getFirst();
        assertThat(savedTask.getId()).isEqualTo(taskId2);
        assertThat(savedTask.getName()).isEqualTo("Task 2");
        assertThat(savedTask.getDescription()).isEqualTo("Description 2");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(savedTask.getId()).isNotEqualTo(taskId1);

        verify(taskRepository, times(1)).saveAll(anyList());
    }
}
