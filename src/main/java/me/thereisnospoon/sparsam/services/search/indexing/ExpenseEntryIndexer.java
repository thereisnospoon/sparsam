package me.thereisnospoon.sparsam.services.search.indexing;

import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;

public interface ExpenseEntryIndexer {

	void addExpenseEntryToIndex(ExpenseEntry expenseEntry);

	void updateIndexedExpenseEntry(ExpenseEntry expenseEntry);

	void deleteExpenseEntryFromIndex(ExpenseCompositeKey expenseEntryCompositeKey);
}
