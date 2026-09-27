package com.amber.splitsmart.auth;
import com.amber.splitsmart.user.AppUser;
import com.amber.splitsmart.user.UserRepository;
import com.amber.splitsmart.invitation.InvitationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder passwords; private final JwtService jwt; private final InvitationService invitations;
    AuthController(UserRepository users, PasswordEncoder passwords, JwtService jwt, InvitationService invitations) { this.users = users; this.passwords = passwords; this.jwt = jwt; this.invitations = invitations; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) AuthResponse register(@Valid @RequestBody Credentials body) {
        if (users.findByEmail(body.email().trim().toLowerCase(Locale.ROOT)).isPresent()) throw new IllegalStateException("An account already exists for this email");
        AppUser user = users.save(new AppUser(body.name(), body.email(), passwords.encode(body.password()))); invitations.acceptPendingInvitations(user); return response(user);
    }
    @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest body) {
        AppUser user = users.findByEmail(body.email().trim().toLowerCase(Locale.ROOT)).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwords.matches(body.password(), user.getPasswordHash())) throw new IllegalArgumentException("Invalid email or password"); return response(user);
    }
    private AuthResponse response(AppUser user) { return new AuthResponse(jwt.createToken(user), user.getId(), user.getName(), user.getEmail()); }
    record Credentials(@NotBlank String name, @Email @NotBlank String email, @NotBlank String password) { }
    record LoginRequest(@Email @NotBlank String email, @NotBlank String password) { }
    record AuthResponse(String accessToken, Long userId, String name, String email) { }
}
