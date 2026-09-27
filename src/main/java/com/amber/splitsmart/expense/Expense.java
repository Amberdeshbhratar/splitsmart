package com.amber.splitsmart.expense;

import com.amber.splitsmart.group.ExpenseGroup;
import com.amber.splitsmart.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private ExpenseGroup group;
    @ManyToOne(optional = false) private AppUser paidBy;
    @Column(nullable = false) private String description;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal totalAmount;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected Expense() { }
    public Expense(ExpenseGroup group, AppUser paidBy, String description, BigDecimal totalAmount) { this.group = group; this.paidBy = paidBy; this.description = description; this.totalAmount = totalAmount; }
    public Long getId() { return id; } public ExpenseGroup getGroup() { return group; } public AppUser getPaidBy() { return paidBy; } public String getDescription() { return description; } public BigDecimal getTotalAmount() { return totalAmount; } public Instant getCreatedAt() { return createdAt; }
    public void revise(AppUser paidBy, String description, BigDecimal totalAmount) { this.paidBy = paidBy; this.description = description; this.totalAmount = totalAmount; }
}
