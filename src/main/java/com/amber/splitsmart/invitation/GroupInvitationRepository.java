package com.amber.splitsmart.invitation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> { List<GroupInvitation> findByInvitedEmailAndStatus(String invitedEmail, InvitationStatus status); }
