package user.service;

import user.dto.NewUserRequest;
import user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest request);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void delete(Long userId);
}
