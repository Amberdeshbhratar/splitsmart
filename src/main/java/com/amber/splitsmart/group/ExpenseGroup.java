package com.amber.splitsmart.group;

import com.amber.splitsmart.user.AppUser;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expense_groups")
public class ExpenseGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @ManyToOne(optional = false) private AppUser createdBy;
    @ManyToMany
    @JoinTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<AppUser> members = new HashSet<>();
    protected ExpenseGroup() { }
    public ExpenseGroup(String name, AppUser createdBy) { this.name = name; this.createdBy = createdBy; members.add(createdBy); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void rename(String name) { this.name = name; }
    public Set<AppUser> getMembers() { return members; }
    public void addMember(AppUser member) { members.add(member); }
    public boolean hasMember(Long userId) { return members.stream().anyMatch(member -> member.getId().equals(userId)); }
}
