package by.timofey.testtask.dto.response;

import by.timofey.testtask.dto.UserDto;
import by.timofey.testtask.entity.enums.TaskStatus;

public record FindTaskResponse(
        String name,
        String description,
        TaskStatus status,
        UserDto user
) {
}
