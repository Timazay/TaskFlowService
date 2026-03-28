package by.timofey.testtask.service;

import by.timofey.testtask.dto.CreateTaskEvent;
import by.timofey.testtask.entity.Task;
import by.timofey.testtask.entity.enums.TaskStatus;
import by.timofey.testtask.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final TaskRepository taskRepository;

    @KafkaListener(topics = "${spring.kafka.topics.task-create-event}",
            groupId = "${spring.kafka.consumer.group-id}",
    batch = "true")
    public void consumeCreateTaskEvent(List<CreateTaskEvent> events,
                                       @Header(KafkaHeaders.RECEIVED_KEY) List<String> strTaskIds,
                                       Acknowledgment acknowledgment) {
        List<UUID> taskIds = strTaskIds.stream()
                .map(key -> UUID.fromString(Objects.requireNonNull(key)))
                .collect(Collectors.toList());

        Map<UUID, Task> existingTasksMap = taskRepository.findAllById(taskIds).stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        List<Task> newTasks = IntStream.range(0, events.size())
                .filter(i ->
                        !existingTasksMap.containsKey(taskIds.get(i)))
                .mapToObj(i -> Task.builder()
                        .id(taskIds.get(i))
                        .name(events.get(i).name())
                        .description(events.get(i).description())
                        .status(TaskStatus.NEW)
                        .build())
                .collect(Collectors.toList());

        if (!newTasks.isEmpty()) {
            taskRepository.saveAll(newTasks);
        }

        acknowledgment.acknowledge();
    }
}
