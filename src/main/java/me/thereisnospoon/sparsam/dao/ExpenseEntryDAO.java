package me.thereisnospoon.sparsam.dao;

import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import java.util.List;

public interface ExpenseEntryDAO {

	ExpenseEntry getExpenseEntryByUsernameAndKey(String parentUsername, String key);

	List<ExpenseEntry> getExpensesForUser(String username);

	void create(ExpenseEntry expenseEntry);

	boolean exists(ExpenseEntry expenseEntry);

	void delete(ExpenseEntry expenseEntry);
}
