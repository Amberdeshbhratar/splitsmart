package com.amber.splitsmart.invitation;
import com.amber.splitsmart.group.ExpenseGroup;
import com.amber.splitsmart.user.AppUser;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class InvitationService {
    private final GroupInvitationRepository invitations; private final ResendEmailService resend; private final GmailEmailService gmail; private final String provider; private final String from;
    InvitationService(GroupInvitationRepository invitations, ResendEmailService resend, GmailEmailService gmail, @Value("${app.mail.provider:console}") String provider, @Value("${app.mail.from:}") String from) { this.invitations = invitations; this.resend = resend; this.gmail = gmail; this.provider = provider; this.from = from; }
    @Transactional public GroupInvitation create(ExpenseGroup group, AppUser inviter, String email) {
        GroupInvitation invitation = invitations.save(new GroupInvitation(group, inviter, email.trim().toLowerCase(Locale.ROOT)));
        String message = "You were invited to join " + group.getName() + ". Sign up with this email to join automatically. Invitation token: " + invitation.getToken();
        if ("resend".equalsIgnoreCase(provider)) resend.send(invitation.getInvitedEmail(), "SplitSmart group invitation", message);
        else if ("gmail".equalsIgnoreCase(provider)) gmail.send(from, invitation.getInvitedEmail(), "SplitSmart group invitation", message);
        else System.out.println("[DEV INVITATION EMAIL] To=" + invitation.getInvitedEmail() + " | " + message);
        return invitation;
    }
    @Transactional public void acceptPendingInvitations(AppUser user) {
        invitations.findByInvitedEmailAndStatus(user.getEmail(), InvitationStatus.PENDING).forEach(invitation -> { if (invitation.getGroup().hasMember(user.getId())) return; invitation.getGroup().addMember(user); invitation.accept(); });
    }
}
