package com.amber.splitsmart.expense;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupId(Long groupId);
    @EntityGraph(attributePaths = "paidBy")
    List<Expense> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
