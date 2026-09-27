package com.amber.splitsmart.invitation;
import com.amber.splitsmart.group.ExpenseGroup;
import com.amber.splitsmart.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity
public class GroupInvitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private ExpenseGroup group;
    @ManyToOne(optional = false) private AppUser invitedBy;
    @Column(nullable = false) private String invitedEmail;
    @Column(nullable = false, unique = true) private String token = UUID.randomUUID().toString();
    @Enumerated(EnumType.STRING) @Column(nullable = false) private InvitationStatus status = InvitationStatus.PENDING;
    @Column(nullable = false) private Instant expiresAt = Instant.now().plusSeconds(7 * 24 * 3600);
    protected GroupInvitation() { }
    public GroupInvitation(ExpenseGroup group, AppUser invitedBy, String invitedEmail) { this.group = group; this.invitedBy = invitedBy; this.invitedEmail = invitedEmail; }
    public ExpenseGroup getGroup() { return group; } public String getInvitedEmail() { return invitedEmail; } public String getToken() { return token; } public InvitationStatus getStatus() { return status; }
    public void accept() { status = InvitationStatus.ACCEPTED; }
}
