package ru.yandex.gateway.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String name;
    @Email(message = "Некорректное значение email")
    private String email;
}
