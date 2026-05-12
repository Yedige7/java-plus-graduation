package user.controller;

import common.dto.UserShortDto;
import common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import user.mapper.UserMapper;
import user.model.User;
import user.repository.UserRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public UserShortDto get(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        return UserMapper.toShortDto(user);
    }

    @GetMapping("/{id}/exists")
    public boolean exists(@PathVariable Long id) {
        return userRepository.existsById(id);
    }
}
