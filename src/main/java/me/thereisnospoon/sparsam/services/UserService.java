package me.thereisnospoon.sparsam.services;

import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import me.thereisnospoon.sparsam.vo.User;

public interface UserService {

	void createUser(User user);

	User getUserByUsername(String username);

	ExpenseEntry addExpenseEntryForUser(Expense expense, String username);

	void updateExpenseEntryForUser(String username, String expenseEntryKey, Expense updatedExpense);
}
