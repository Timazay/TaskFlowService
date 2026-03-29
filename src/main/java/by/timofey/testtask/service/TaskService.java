package by.timofey.testtask.service;

import by.timofey.testtask.config.KafkaPropertyConfig;
import by.timofey.testtask.dto.AssignUserToTaskEvent;
import by.timofey.testtask.dto.request.ChangeTaskStatusRequest;
import by.timofey.testtask.dto.request.CreateTaskRequest;
import by.timofey.testtask.dto.response.CreateTaskResponse;
import by.timofey.testtask.dto.response.FindAllTasksResponse;
import by.timofey.testtask.dto.response.FindTaskResponse;
import by.timofey.testtask.dto.UserDto;
import by.timofey.testtask.entity.Task;
import by.timofey.testtask.entity.User;
import by.timofey.testtask.entity.enums.TaskStatus;
import by.timofey.testtask.exception.ConflictException;
import by.timofey.testtask.exception.NotFoundException;
import by.timofey.testtask.repository.TaskRepository;
import by.timofey.testtask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaPropertyConfig kafkaConfig;
    private final UserRepository userRepository;

    public FindTaskResponse findByTaskId(UUID taskId) {
        Task task = taskRepository.findFullTaskById(taskId).orElseThrow(() -> new NotFoundException("Task not found"));
        UserDto userDto = Optional.ofNullable(task.getUser())
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail()))
                .orElse(null);
        return new FindTaskResponse(task.getName(), task.getDescription(), task.getStatus(), userDto);
    }

    public List<FindAllTasksResponse> findAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        List<Task> tasks = taskRepository.findAll(pageable).stream().toList();
        return tasks.stream()
                .map(task -> new FindAllTasksResponse(task.getId(), task.getName(), task.getDescription()))
                .toList();
    }

    @Transactional(transactionManager = "transactionManager")
    public CreateTaskResponse createTask(CreateTaskRequest request) {
        Task task = Task.builder()
                .name(request.name())
                .description(request.description())
                .status(TaskStatus.NEW)
                .build();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                kafkaConfig.getTopics().taskCreateEvent(),
                request
        );

        Task savedTask = taskRepository.save(task);
        kafkaTemplate.send(record);

        return new CreateTaskResponse(savedTask.getId());
    }

    public void changeTaskStatus(UUID taskId, ChangeTaskStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        task.setStatus(request.status());
        taskRepository.save(task);
    }

    @Transactional(transactionManager = "transactionManager")
    public void assignUserToTask(AssignUserToTaskEvent event) {
        Task task = taskRepository.findById(event.taskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));

        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (task.getUser() != null && task.getUser().equals(user))
            throw new ConflictException("Task is already assigned to this user");

        task.setUser(user);

        taskRepository.save(task);
        kafkaTemplate.send(kafkaConfig.getTopics().taskAssignUser(), event);
    }
}
