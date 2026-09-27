package com.amber.splitsmart.group;

import com.amber.splitsmart.user.AppUser;
import com.amber.splitsmart.user.UserRepository;
import com.amber.splitsmart.invitation.InvitationService;
import com.amber.splitsmart.user.UserController.UserSummary;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/groups")
public class GroupController {
    private final GroupRepository groups; private final UserRepository users; private final InvitationService invitations;
    GroupController(GroupRepository groups, UserRepository users, InvitationService invitations) { this.groups = groups; this.users = users; this.invitations = invitations; }
    @GetMapping
    List<GroupSummary> list(@RequestAttribute("currentUser") AppUser currentUser) {
        return groups.findByMembers_IdOrderByIdDesc(currentUser.getId()).stream()
            .map(group -> new GroupSummary(group.getId(), group.getName(), group.getMembers().size()))
            .toList();
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    ExpenseGroup create(@RequestAttribute("currentUser") AppUser creator, @Valid @RequestBody CreateGroupRequest request) {
        return groups.save(new ExpenseGroup(request.name(), creator));
    }
    @PutMapping("/{groupId}")
    GroupSummary rename(@PathVariable Long groupId, @Valid @RequestBody CreateGroupRequest request) {
        ExpenseGroup group = findGroup(groupId); group.rename(request.name()); groups.save(group);
        return new GroupSummary(group.getId(), group.getName(), group.getMembers().size());
    }
    @PostMapping("/{groupId}/members")
    AddMemberResponse addMember(@PathVariable Long groupId, @RequestAttribute("currentUser") AppUser inviter, @Valid @RequestBody AddMemberRequest request) {
        ExpenseGroup group = findGroup(groupId);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        var existingUser = users.findByEmail(email);
        if (existingUser.isPresent()) { group.addMember(existingUser.get()); groups.save(group); return new AddMemberResponse("ADDED", "Registered user added to group"); }
        var invitation = invitations.create(group, inviter, email); return new AddMemberResponse("INVITED", "Invitation created", invitation.getToken());
    }
    @GetMapping("/{groupId}/members")
    List<UserSummary> members(@PathVariable Long groupId) {
        return findGroup(groupId).getMembers().stream().map(user -> new UserSummary(user.getId(), user.getName(), user.getEmail())).toList();
    }
    ExpenseGroup findGroup(Long id) { return groups.findWithMembersById(id).orElseThrow(() -> new IllegalArgumentException("Group not found")); }
    record CreateGroupRequest(@NotBlank String name) { }
    record AddMemberRequest(@Email @NotBlank String email) { }
    record AddMemberResponse(String status, String message, String invitationToken) { AddMemberResponse(String status, String message) { this(status, message, null); } }
    record GroupSummary(Long id, String name, int memberCount) { }
}
