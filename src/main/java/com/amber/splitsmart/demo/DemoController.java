package com.amber.splitsmart.demo;

import com.amber.splitsmart.expense.ExpenseService;
import com.amber.splitsmart.expense.SplitType;
import com.amber.splitsmart.group.ExpenseGroup;
import com.amber.splitsmart.group.GroupRepository;
import com.amber.splitsmart.user.AppUser;
import com.amber.splitsmart.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/** Creates deliberately fictional data for portfolio screenshots in a local environment. */
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    private final UserRepository users; private final GroupRepository groups; private final ExpenseService expenses; private final PasswordEncoder passwords;
    DemoController(UserRepository users, GroupRepository groups, ExpenseService expenses, PasswordEncoder passwords) { this.users = users; this.groups = groups; this.expenses = expenses; this.passwords = passwords; }

    @PostMapping("/screenshot-data")
    DemoResponse create(@RequestAttribute("currentUser") AppUser current) {
        AppUser aarav = user("Aarav Mehta", "aarav.demo@splitsmart.local");
        AppUser nisha = user("Nisha Kapoor", "nisha.demo@splitsmart.local");
        ExpenseGroup goa = new ExpenseGroup("Goa Weekend", current); goa.addMember(aarav); goa.addMember(nisha); goa = groups.save(goa);
        List<String> everyone = List.of(current.getEmail(), aarav.getEmail(), nisha.getEmail());
        expenses.createExpense(goa.getId(), "Beach house", new BigDecimal("9000"), current.getId(), everyone, SplitType.EQUAL, Map.of());
        expenses.createExpense(goa.getId(), "Road trip fuel", new BigDecimal("1800"), aarav.getId(), everyone, SplitType.SHARES, Map.of(current.getEmail(), new BigDecimal("1"), aarav.getEmail(), new BigDecimal("1"), nisha.getEmail(), new BigDecimal("2")));
        expenses.createExpense(goa.getId(), "Dinner at Thalassa", new BigDecimal("3600"), nisha.getId(), everyone, SplitType.EXACT, Map.of(current.getEmail(), new BigDecimal("1200"), aarav.getEmail(), new BigDecimal("900"), nisha.getEmail(), new BigDecimal("1500")));
        expenses.createExpense(goa.getId(), "Snacks and water", new BigDecimal("750"), current.getId(), everyone, SplitType.ADJUSTMENT, Map.of(current.getEmail(), new BigDecimal("50"), aarav.getEmail(), BigDecimal.ZERO, nisha.getEmail(), new BigDecimal("-50")));
        ExpenseGroup flat = new ExpenseGroup("Flat 402", current); flat.addMember(aarav); groups.save(flat);
        return new DemoResponse(goa.getId(), "Sample groups and expenses are ready.");
    }

    private AppUser user(String name, String email) { return users.findByEmail(email).orElseGet(() -> users.save(new AppUser(name, email, passwords.encode("demo-only-password")))); }
    record DemoResponse(Long groupId, String message) { }
}
