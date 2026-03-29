package by.timofey.testtask.dto;

import java.util.UUID;

public record UserDto(
        UUID userId,
        String name,
        String email
) {
}
