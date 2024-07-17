package ru.yandex.gateway.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    @NotBlank
    @NotNull
    private String text;
}