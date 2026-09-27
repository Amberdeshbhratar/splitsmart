package com.amber.splitsmart.balance;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Component;

/** Greedily pairs largest debtors and creditors to minimise suggested transfers. */
@Component
public class DebtSimplificationEngine {
    public List<SettlementSuggestion> simplify(Map<Long, BigDecimal> netBalances) {
        PriorityQueue<Balance> creditors = new PriorityQueue<>(Comparator.comparing(Balance::amount).reversed());
        PriorityQueue<Balance> debtors = new PriorityQueue<>(Comparator.comparing(Balance::amount).reversed());
        netBalances.forEach((userId, net) -> { if (net.signum() > 0) creditors.add(new Balance(userId, net)); else if (net.signum() < 0) debtors.add(new Balance(userId, net.negate())); });
        List<SettlementSuggestion> suggestions = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance creditor = creditors.remove(), debtor = debtors.remove();
            BigDecimal amount = creditor.amount.min(debtor.amount);
            suggestions.add(new SettlementSuggestion(debtor.userId, creditor.userId, amount));
            if (creditor.amount.compareTo(amount) > 0) creditors.add(new Balance(creditor.userId, creditor.amount.subtract(amount)));
            if (debtor.amount.compareTo(amount) > 0) debtors.add(new Balance(debtor.userId, debtor.amount.subtract(amount)));
        }
        return suggestions;
    }
    private record Balance(Long userId, BigDecimal amount) { }
    public record SettlementSuggestion(Long payerUserId, Long payeeUserId, BigDecimal amount) { }
}
