package com.amber.splitsmart.expense;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {
    private final ExpenseService service;
    ExpenseController(ExpenseService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    Expense create(@PathVariable Long groupId, @Valid @RequestBody CreateExpenseRequest request) { return service.createExpense(groupId, request.description(), request.totalAmount(), request.paidByUserId(), request.participantEmails(), request.splitType(), request.allocations()); }
    @PutMapping("/{expenseId}")
    Expense update(@PathVariable Long groupId, @PathVariable Long expenseId, @Valid @RequestBody CreateExpenseRequest request) { return service.updateExpense(groupId, expenseId, request.description(), request.totalAmount(), request.paidByUserId(), request.participantEmails(), request.splitType(), request.allocations()); }
    @GetMapping
    @Transactional(readOnly = true)
    List<ExpenseSummary> history(@PathVariable Long groupId) {
        return service.history(groupId);
    }
    record CreateExpenseRequest(@NotBlank String description, @NotNull BigDecimal totalAmount, @NotNull Long paidByUserId, @NotEmpty List<String> participantEmails, SplitType splitType, java.util.Map<String, BigDecimal> allocations) { }
    record ExpenseSummary(Long id, String description, BigDecimal totalAmount, Long paidByUserId, String paidByName, String paidByEmail, java.time.Instant createdAt) { }
}
