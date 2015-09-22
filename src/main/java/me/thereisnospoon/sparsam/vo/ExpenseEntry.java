package me.thereisnospoon.sparsam.vo;

public class ExpenseEntry {

	private Expense expense;
	private ExpenseCompositeKey expenseCompositeKey;

	public Expense getExpense() {
		return expense;
	}

	public void setExpense(Expense expense) {
		this.expense = expense;
	}

	public ExpenseCompositeKey getExpenseCompositeKey() {
		return expenseCompositeKey;
	}

	public void setExpenseCompositeKey(ExpenseCompositeKey expenseCompositeKey) {
		this.expenseCompositeKey = expenseCompositeKey;
	}
}
