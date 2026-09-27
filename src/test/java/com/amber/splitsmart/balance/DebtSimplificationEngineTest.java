package com.amber.splitsmart.balance;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
class DebtSimplificationEngineTest {
    private final DebtSimplificationEngine engine = new DebtSimplificationEngine();
    @Test void pairs_debtors_with_creditors() {
        var result = engine.simplify(Map.of(1L, new BigDecimal("1500.00"), 2L, new BigDecimal("-1000.00"), 3L, new BigDecimal("-500.00")));
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("1500.00"), result.stream().map(DebtSimplificationEngine.SettlementSuggestion::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
