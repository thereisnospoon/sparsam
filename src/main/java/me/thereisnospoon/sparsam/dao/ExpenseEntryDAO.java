package me.thereisnospoon.sparsam.dao;

import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import java.util.List;

public interface ExpenseEntryDAO {

	ExpenseEntry getExpenseEntryByCompositeKey(ExpenseCompositeKey expenseCompositeKey);

	List<ExpenseEntry> getExpensesForUser(String username);

	void create(ExpenseEntry expenseEntry);

	boolean exists(ExpenseCompositeKey expenseCompositeKey);

	void delete(ExpenseCompositeKey expenseCompositeKey);

	void update(ExpenseEntry expenseEntry);
}
