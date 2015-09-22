package me.thereisnospoon.sparsam.dao.impl;

import com.google.common.base.Preconditions;
import me.thereisnospoon.sparsam.dao.ExpenseEntryDAO;
import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.exceptions.dao.NoSuchEntityException;
import me.thereisnospoon.sparsam.vo.Expense;
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
	public ExpenseEntry getExpenseEntryByUsernameAndKey(String parentUsername, String key) {

		checkExpenseUsernameAndKeyNotEmpty(parentUsername, key);
		return redisHashOperations.get(getFullNameForRedisCollectionForExpenses(parentUsername), key);
	}

	public List<ExpenseEntry> getExpensesForUser(String username) {

		Preconditions.checkArgument(!StringUtils.isEmpty(username));
		return redisHashOperations.values(getFullNameForRedisCollectionForExpenses(username));
	}

	private String getFullNameForRedisCollectionForExpenses(String parentUsername) {
		return redisCollectionNamePrefixForEntries + parentUsername;
	}

	private void checkExpenseUsernameAndKeyNotEmpty(String parentUsername, String key) {

		Preconditions.checkArgument(!StringUtils.isEmpty(parentUsername));
		Preconditions.checkArgument(!StringUtils.isEmpty(key));
	}

	private void checkIfExpenseEntryIsValidForStorage(ExpenseEntry expenseEntry) {

		checkExpenseUsernameAndKeyNotEmpty(expenseEntry.getUsername(), expenseEntry.getUniqueKey());
		Preconditions.checkNotNull(expenseEntry.getDateOfExpense());

		Expense expense = expenseEntry.getExpense();

		Preconditions.checkNotNull(expense);
		Preconditions.checkNotNull(expense.getCurrency());
		Preconditions.checkArgument(!StringUtils.isEmpty(expense.getDescription()));
		Preconditions.checkNotNull(expense.getAmount());
	}

	@Override
	public void create(ExpenseEntry expenseEntry) {

		checkIfExpenseEntryIsValidForStorage(expenseEntry);

		if (exists(expenseEntry)) {
			throw new EntityAlreadyExistsException(String.format("Expense entry with key '%s' for user '%s' " +
					"already exists", expenseEntry.getUniqueKey(), expenseEntry.getUsername()));
		}

		redisHashOperations.put(getFullNameForRedisCollectionForExpenses(expenseEntry.getUsername()), expenseEntry.getUniqueKey(), expenseEntry);
	}

	@Override
	public boolean exists(ExpenseEntry expenseEntry) {

		checkExpenseUsernameAndKeyNotEmpty(expenseEntry.getUsername(), expenseEntry.getUniqueKey());
		return redisHashOperations.hasKey(getFullNameForRedisCollectionForExpenses(expenseEntry.getUsername()), expenseEntry.getUniqueKey());
	}

	@Override
	public void delete(ExpenseEntry expenseEntry) {

		checkExpenseUsernameAndKeyNotEmpty(expenseEntry.getUsername(), expenseEntry.getUniqueKey());
		redisHashOperations.delete(getFullNameForRedisCollectionForExpenses(expenseEntry.getUsername()), expenseEntry.getUniqueKey());
	}

	@Override
	public void update(ExpenseEntry expenseEntry) {

		checkExpenseUsernameAndKeyNotEmpty(expenseEntry.getUsername(), expenseEntry.getUniqueKey());
		if (!exists(expenseEntry)) {
			throw new NoSuchEntityException("There is no expense entry with key '%s' for user '%s' in DB");
		}

		redisHashOperations.put(getFullNameForRedisCollectionForExpenses(expenseEntry.getUsername()),
				expenseEntry.getUniqueKey(), expenseEntry);
	}
}
