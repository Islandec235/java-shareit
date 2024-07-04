package ru.practicum.shareit.request.dto;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;

@Component
public class ItemRequestMapper {
    public ItemRequestDto toItemRequestDto(@NonNull ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated()
        );
    }

    public ItemRequest toItemRequest(@NonNull ItemRequestDto itemRequestDto) {
        return new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                itemRequestDto.getCreated()
        );
    }
}
