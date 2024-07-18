package ru.yandex.gateway.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookItemRequestDto {
    private long itemId;
    @FutureOrPresent
    @NotNull
    private LocalDateTime start;
    @NotNull
    @Future
    private LocalDateTime end;

    public boolean dateValid() {
        return !start.isAfter(end) && !start.isEqual(end);
    }
}
