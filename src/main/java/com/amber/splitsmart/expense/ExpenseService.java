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
    public Expense createEqualExpense(Long groupId, String description, BigDecimal totalAmount, Long paidByUserId, List<String> participantEmails) {
        return saveEqualExpense(groupId, null, description, totalAmount, paidByUserId, participantEmails);
    }

    @Transactional
    public Expense updateEqualExpense(Long groupId, Long expenseId, String description, BigDecimal totalAmount, Long paidByUserId, List<String> participantEmails) {
        Expense existing = expenses.findById(expenseId).orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        if (!existing.getGroup().getId().equals(groupId)) throw new IllegalArgumentException("Expense does not belong to this group");
        return saveEqualExpense(groupId, existing, description, totalAmount, paidByUserId, participantEmails);
    }

    private Expense saveEqualExpense(Long groupId, Expense existing, String description, BigDecimal totalAmount, Long paidByUserId, List<String> participantEmails) {
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
        BigDecimal share = totalAmount.divide(BigDecimal.valueOf(participants.size()), 2, RoundingMode.DOWN);
        BigDecimal remainder = totalAmount.subtract(share.multiply(BigDecimal.valueOf(participants.size()))).setScale(2, RoundingMode.HALF_UP);
        List<ExpenseSplit> expenseSplits = new ArrayList<>();
        for (int index = 0; index < participants.size(); index++) {
            BigDecimal owed = index == 0 ? share.add(remainder) : share;
            expenseSplits.add(new ExpenseSplit(expense, participants.get(index), owed));
        }
        splits.saveAll(expenseSplits);
        return expense;
    }

    @Transactional(readOnly = true)
    public List<ExpenseController.ExpenseSummary> history(Long groupId) {
        return expenses.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
            .map(expense -> new ExpenseController.ExpenseSummary(expense.getId(), expense.getDescription(), expense.getTotalAmount(), expense.getPaidBy().getName(), expense.getPaidBy().getEmail(), expense.getCreatedAt()))
            .toList();
    }
}
