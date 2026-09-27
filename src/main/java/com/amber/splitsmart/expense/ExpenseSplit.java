package com.amber.splitsmart.expense;
import com.amber.splitsmart.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
public class ExpenseSplit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Expense expense;
    @ManyToOne(optional = false) private AppUser user;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal owedAmount;
    protected ExpenseSplit() { }
    public ExpenseSplit(Expense expense, AppUser user, BigDecimal owedAmount) { this.expense = expense; this.user = user; this.owedAmount = owedAmount; }
    public Expense getExpense() { return expense; } public AppUser getUser() { return user; } public BigDecimal getOwedAmount() { return owedAmount; }
}
