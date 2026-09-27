package com.amber.splitsmart.group;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GroupRepository extends JpaRepository<ExpenseGroup, Long> {
    @EntityGraph(attributePaths = "members")
    Optional<ExpenseGroup> findWithMembersById(Long id);
    @EntityGraph(attributePaths = "members")
    List<ExpenseGroup> findByMembers_IdOrderByIdDesc(Long userId);
}
