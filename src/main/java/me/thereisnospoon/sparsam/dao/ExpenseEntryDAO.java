package me.thereisnospoon.sparsam.dao;

import com.google.common.base.Preconditions;
import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.util.StringUtils;

import java.util.List;

public class ExpenseEntryDAO implements GenericDAO<ExpenseEntry> {

	private String redisCollectionNamePrefixForEntries;
	private HashOperations<String,String,ExpenseEntry> redisHashOperations;

	public void setRedisCollectionNamePrefixForEntries(String redisCollectionNamePrefixForEntries) {
		this.redisCollectionNamePrefixForEntries = redisCollectionNamePrefixForEntries;
	}

	public void setRedisHashOperations(HashOperations<String, String, ExpenseEntry> redisHashOperations) {
		this.redisHashOperations = redisHashOperations;
	}



	@Override
	public ExpenseEntry getEntryByKey(String key) {
		throw new UnsupportedOperationException();
	}

	public ExpenseEntry loadEntry(ExpenseEntry expenseEntry) {

		checkIfExpenseEntryCouldBeIdentified(expenseEntry);
		return redisHashOperations.get(getFullRedisCollectionForExpensesName(expenseEntry.getUsername()), expenseEntry.getUniqueKey());
	}

	public List<ExpenseEntry> getExpensesForUser(String username) {

		Preconditions.checkArgument(!StringUtils.isEmpty(username));
		return redisHashOperations.values(getFullRedisCollectionForExpensesName(username));
	}

	private String getFullRedisCollectionForExpensesName(String username) {
		return redisCollectionNamePrefixForEntries + username;
	}

	private void checkIfExpenseEntryCouldBeIdentified(ExpenseEntry expenseEntry) {

		Preconditions.checkNotNull(expenseEntry);
		Preconditions.checkArgument(!StringUtils.isEmpty(expenseEntry.getUsername()));
		Preconditions.checkArgument(!StringUtils.isEmpty(expenseEntry.getUniqueKey()));
	}

	private void checkIfExpenseEntryIsValidForStorage(ExpenseEntry expenseEntry) {

		checkIfExpenseEntryCouldBeIdentified(expenseEntry);

		Preconditions.checkNotNull(expenseEntry.getCurrency());
		Preconditions.checkNotNull(expenseEntry.getDateOfExpense());
		Preconditions.checkArgument(!StringUtils.isEmpty(expenseEntry.getDescription()));
		Preconditions.checkNotNull(expenseEntry.getAmount());
	}

	@Override
	public void create(ExpenseEntry expenseEntry) {

		checkIfExpenseEntryIsValidForStorage(expenseEntry);

		if (exists(expenseEntry)) {
			throw new EntityAlreadyExistsException(String.format("Expense entry with key '%s' for user '%s' " +
					"already exists", expenseEntry.getUniqueKey(), expenseEntry.getUsername()));
		}

		redisHashOperations.put(getFullRedisCollectionForExpensesName(expenseEntry.getUsername()), expenseEntry.getUniqueKey(), expenseEntry);
	}

	@Override
	public boolean exists(ExpenseEntry expenseEntry) {

		checkIfExpenseEntryCouldBeIdentified(expenseEntry);
		return redisHashOperations.hasKey(getFullRedisCollectionForExpensesName(expenseEntry.getUsername()), expenseEntry.getUniqueKey());
	}

	@Override
	public void delete(ExpenseEntry expenseEntry) {

		checkIfExpenseEntryCouldBeIdentified(expenseEntry);
		redisHashOperations.delete(getFullRedisCollectionForExpensesName(expenseEntry.getUsername()), expenseEntry.getUniqueKey());
	}
}
