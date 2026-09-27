package com.amber.splitsmart.balance;
import com.amber.splitsmart.expense.*;
import com.amber.splitsmart.group.*;
import com.amber.splitsmart.user.AppUser;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/groups/{groupId}")
public class BalanceController {
    private final GroupRepository groups; private final ExpenseRepository expenses; private final ExpenseSplitRepository splits; private final DebtSimplificationEngine engine;
    BalanceController(GroupRepository groups, ExpenseRepository expenses, ExpenseSplitRepository splits, DebtSimplificationEngine engine) { this.groups = groups; this.expenses = expenses; this.splits = splits; this.engine = engine; }
    @GetMapping("/balances") Map<Long, BigDecimal> balances(@PathVariable Long groupId) { return calculate(groupId); }
    @GetMapping("/settlement-suggestions") List<DebtSimplificationEngine.SettlementSuggestion> suggestions(@PathVariable Long groupId) { return engine.simplify(calculate(groupId)); }
    private Map<Long, BigDecimal> calculate(Long groupId) {
        ExpenseGroup group = groups.findWithMembersById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
        Map<Long, BigDecimal> net = new LinkedHashMap<>();
        for (AppUser member : group.getMembers()) net.put(member.getId(), BigDecimal.ZERO.setScale(2));
        for (Expense expense : expenses.findByGroupId(groupId)) net.compute(expense.getPaidBy().getId(), (id, amount) -> amount.add(expense.getTotalAmount()));
        for (ExpenseSplit split : splits.findAllByGroupId(groupId)) net.compute(split.getUser().getId(), (id, amount) -> amount.subtract(split.getOwedAmount()));
        return net;
    }
}
