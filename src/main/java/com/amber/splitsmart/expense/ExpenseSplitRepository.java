package com.amber.splitsmart.expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    @Query("select split from ExpenseSplit split where split.expense.group.id = :groupId")
    List<ExpenseSplit> findAllByGroupId(@Param("groupId") Long groupId);
    void deleteByExpenseId(Long expenseId);
}
