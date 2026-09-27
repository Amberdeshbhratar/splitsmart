package com.amber.splitsmart.expense;

import com.amber.splitsmart.group.ExpenseGroup;
import com.amber.splitsmart.group.GroupRepository;
import com.amber.splitsmart.user.AppUser;
import com.amber.splitsmart.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {
    private final GroupRepository groups; private final UserRepository users; private final ExpenseRepository expenses; private final ExpenseSplitRepository splits;
    public ExpenseService(GroupRepository groups, UserRepository users, ExpenseRepository expenses, ExpenseSplitRepository splits) { this.groups = groups; this.users = users; this.expenses = expenses; this.splits = splits; }

    @Transactional
    public Expense createExpense(Long groupId, String description, BigDecimal totalAmount, Long paidByUserId, List<String> participantEmails, SplitType splitType, java.util.Map<String, BigDecimal> allocations) {
        return saveExpense(groupId, null, description, totalAmount, paidByUserId, participantEmails, splitType, allocations);
    }

    @Transactional
    public Expense updateExpense(Long groupId, Long expenseId, String description, BigDecimal totalAmount, Long paidByUserId, List<String> participantEmails, SplitType splitType, java.util.Map<String, BigDecimal> allocations) {
        Expense existing = expenses.findById(expenseId).orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        if (!existing.getGroup().getId().equals(groupId)) throw new IllegalArgumentException("Expense does not belong to this group");
        return saveExpense(groupId, existing, description, totalAmount, paidByUserId, participantEmails, splitType, allocations);
    }

    @Transactional
    public Expense recordSettlement(Long groupId, Long payerUserId, Long payeeUserId, BigDecimal amount) {
        AppUser payee = users.findById(payeeUserId).orElseThrow(() -> new IllegalArgumentException("Payee not found"));
        return createExpense(groupId, "Settlement", amount, payerUserId, List.of(payee.getEmail()), SplitType.EXACT, java.util.Map.of(payee.getEmail(), amount));
    }

    private Expense saveExpense(Long groupId, Expense existing, String description, BigDecimal totalAmount, Long paidByUserId, List<String> participantEmails, SplitType splitType, java.util.Map<String, BigDecimal> allocations) {
        if (totalAmount == null || totalAmount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        if (participantEmails == null || participantEmails.isEmpty()) throw new IllegalArgumentException("At least one participant is required");
        ExpenseGroup group = groups.findWithMembersById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
        AppUser payer = users.findById(paidByUserId).orElseThrow(() -> new IllegalArgumentException("Payer not found"));
        if (!group.hasMember(payer.getId())) throw new IllegalArgumentException("Payer must be a group member");
        List<AppUser> participants = participantEmails.stream().map(email -> email.trim().toLowerCase(Locale.ROOT)).distinct().map(email -> users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Participant not found: " + email))).toList();
        if (participants.stream().anyMatch(user -> !group.hasMember(user.getId()))) throw new IllegalArgumentException("Every participant must belong to the group");
        Expense expense = existing == null ? new Expense(group, payer, description, totalAmount.setScale(2, RoundingMode.HALF_UP)) : existing;
        if (existing != null) { existing.revise(payer, description, totalAmount.setScale(2, RoundingMode.HALF_UP)); splits.deleteByExpenseId(existing.getId()); }
        expense = expenses.save(expense);
        List<BigDecimal> amounts = calculateAmounts(totalAmount, participants, splitType == null ? SplitType.EQUAL : splitType, allocations == null ? java.util.Map.of() : allocations);
        List<ExpenseSplit> expenseSplits = new ArrayList<>();
        for (int index = 0; index < participants.size(); index++) {
            expenseSplits.add(new ExpenseSplit(expense, participants.get(index), amounts.get(index)));
        }
        splits.saveAll(expenseSplits);
        return expense;
    }

    private List<BigDecimal> calculateAmounts(BigDecimal total, List<AppUser> people, SplitType type, java.util.Map<String, BigDecimal> raw) {
        List<BigDecimal> values = people.stream().map(p -> raw.getOrDefault(p.getEmail(), BigDecimal.ZERO)).toList();
        if (type == SplitType.EXACT) {
            if (values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(total) != 0) throw new IllegalArgumentException("Exact amounts must add up to the total");
            if (values.stream().anyMatch(v -> v.signum() < 0)) throw new IllegalArgumentException("Split amounts cannot be negative");
            return values;
        }
        if (type == SplitType.ADJUSTMENT) {
            BigDecimal base = total.subtract(values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)).divide(BigDecimal.valueOf(people.size()), 2, RoundingMode.DOWN);
            List<BigDecimal> result = new ArrayList<>();
            for (BigDecimal adjustment : values) { BigDecimal amount = base.add(adjustment); if (amount.signum() < 0) throw new IllegalArgumentException("An adjustment makes a share negative"); result.add(amount); }
            return distributeRemainder(total, result);
        }
        if (type == SplitType.SHARES) {
            if (values.stream().anyMatch(v -> v.signum() <= 0)) throw new IllegalArgumentException("Each person needs at least one share");
            BigDecimal units = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            return distributeRemainder(total, values.stream().map(v -> total.multiply(v).divide(units, 2, RoundingMode.DOWN)).toList());
        }
        BigDecimal each = total.divide(BigDecimal.valueOf(people.size()), 2, RoundingMode.DOWN);
        return distributeRemainder(total, java.util.Collections.nCopies(people.size(), each));
    }

    private List<BigDecimal> distributeRemainder(BigDecimal total, List<BigDecimal> preliminary) {
        List<BigDecimal> result = new ArrayList<>(preliminary);
        BigDecimal remainder = total.subtract(result.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        result.set(0, result.get(0).add(remainder));
        return result;
    }

    @Transactional(readOnly = true)
    public List<ExpenseController.ExpenseSummary> history(Long groupId) {
        return expenses.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
            .map(expense -> new ExpenseController.ExpenseSummary(expense.getId(), expense.getDescription(), expense.getTotalAmount(), expense.getPaidBy().getId(), expense.getPaidBy().getName(), expense.getPaidBy().getEmail(), expense.getCreatedAt()))
            .toList();
    }
}
