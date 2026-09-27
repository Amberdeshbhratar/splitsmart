package com.amber.splitsmart.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/users")
public class UserController {
    private final UserRepository users; private final PasswordEncoder passwords;
    UserController(UserRepository users, PasswordEncoder passwords) { this.users = users; this.passwords = passwords; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    AppUser create(@Valid @RequestBody CreateUserRequest request) {
        if (users.findByEmail(request.email().trim().toLowerCase()).isPresent()) throw new IllegalStateException("An account already exists for this email");
        return users.save(new AppUser(request.name(), request.email(), passwords.encode(request.password())));
    }
    @GetMapping("/search")
    List<UserSummary> search(@RequestParam String q) {
        if (q.trim().length() < 2) return List.of();
        return users.findTop8ByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q.trim(), q.trim()).stream().map(user -> new UserSummary(user.getId(), user.getName(), user.getEmail())).toList();
    }
    public record UserSummary(Long id, String name, String email) { }
    record CreateUserRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank String password) { }
}
