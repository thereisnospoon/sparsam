package me.thereisnospoon.sparsam.dao.impl;

import com.google.common.base.Preconditions;
import me.thereisnospoon.sparsam.dao.ExpenseEntryDAO;
import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.exceptions.dao.NoSuchEntityException;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.util.StringUtils;

import java.util.List;

public class ExpenseEntryDAOImpl implements ExpenseEntryDAO {

	private String redisCollectionNamePrefixForEntries;
	private HashOperations<String,String,ExpenseEntry> redisHashOperations;

	public void setRedisCollectionNamePrefixForEntries(String redisCollectionNamePrefixForEntries) {
		this.redisCollectionNamePrefixForEntries = redisCollectionNamePrefixForEntries;
	}

	public void setRedisHashOperations(HashOperations<String, String, ExpenseEntry> redisHashOperations) {
		this.redisHashOperations = redisHashOperations;
	}

	@Override
	public ExpenseEntry getExpenseEntryByCompositeKey(ExpenseCompositeKey expenseCompositeKey) {

		checkExpenseCompositeKey(expenseCompositeKey);
		return redisHashOperations.get(getFullNameForRedisCollectionForExpenses(expenseCompositeKey.getUsername()),
				expenseCompositeKey.getUniqueKey());
	}

	public List<ExpenseEntry> getExpensesForUser(String username) {

		Preconditions.checkArgument(!StringUtils.isEmpty(username));
		return redisHashOperations.values(getFullNameForRedisCollectionForExpenses(username));
	}

	private String getFullNameForRedisCollectionForExpenses(String parentUsername) {
		return redisCollectionNamePrefixForEntries + parentUsername;
	}

	private void checkExpenseCompositeKey(ExpenseCompositeKey expenseCompositeKey) {

		Preconditions.checkArgument(!StringUtils.isEmpty(expenseCompositeKey.getUsername()));
		Preconditions.checkArgument(!StringUtils.isEmpty(expenseCompositeKey.getUniqueKey()));
	}

	private void checkIfExpenseEntryIsValidForStorage(ExpenseEntry expenseEntry) {

		checkExpenseCompositeKey(expenseEntry.getExpenseCompositeKey());

		Expense expense = expenseEntry.getExpense();

		Preconditions.checkNotNull(expense.getDateOfExpense());
		Preconditions.checkNotNull(expense);
		Preconditions.checkNotNull(expense.getCurrency());
		Preconditions.checkArgument(!StringUtils.isEmpty(expense.getDescription()));
		Preconditions.checkNotNull(expense.getAmount());
	}

	@Override
	public void create(ExpenseEntry expenseEntry) {

		checkIfExpenseEntryIsValidForStorage(expenseEntry);

		ExpenseCompositeKey expenseCompositeKey = expenseEntry.getExpenseCompositeKey();

		if (exists(expenseEntry.getExpenseCompositeKey())) {
			throw new EntityAlreadyExistsException(String.format("Expense entry with key '%s' for user '%s' " +
					"already exists", expenseCompositeKey.getUniqueKey(),
					expenseCompositeKey.getUsername()));
		}

		redisHashOperations.put(getFullNameForRedisCollectionForExpenses(expenseCompositeKey.getUsername()), expenseCompositeKey.getUniqueKey(), expenseEntry);
	}

	@Override
	public boolean exists(ExpenseCompositeKey expenseCompositeKey) {

		checkExpenseCompositeKey(expenseCompositeKey);
		return redisHashOperations.hasKey(getFullNameForRedisCollectionForExpenses(expenseCompositeKey.getUsername()),
				expenseCompositeKey.getUniqueKey());
	}

	@Override
	public void delete(ExpenseCompositeKey expenseCompositeKey) {

		checkExpenseCompositeKey(expenseCompositeKey);
		redisHashOperations.delete(getFullNameForRedisCollectionForExpenses(expenseCompositeKey.getUsername()),
				expenseCompositeKey.getUniqueKey());
	}

	@Override
	public void update(ExpenseEntry expenseEntry) {

		ExpenseCompositeKey expenseCompositeKey = expenseEntry.getExpenseCompositeKey();

		checkExpenseCompositeKey(expenseCompositeKey);
		if (!exists(expenseCompositeKey)) {
			throw new NoSuchEntityException("There is no expense entry with key '%s' for user '%s' in DB");
		}

		redisHashOperations.put(getFullNameForRedisCollectionForExpenses(expenseCompositeKey.getUsername()),
				expenseCompositeKey.getUniqueKey(), expenseEntry);
	}
}
