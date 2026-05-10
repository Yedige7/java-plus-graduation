package user.mapper;

import user.dto.NewUserRequest;
import user.dto.UserDto;
import common.dto.UserShortDto;
import user.model.User;

public class UserMapper {

    public static User toEntity(NewUserRequest dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        return user;
    }

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    public static UserShortDto toShortDto(User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getUserId());
        dto.setName(user.getName());
        return dto;
    }
}