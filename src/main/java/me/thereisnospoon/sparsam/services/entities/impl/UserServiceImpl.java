package me.thereisnospoon.sparsam.services.entities.impl;

import me.thereisnospoon.sparsam.dao.ExpenseEntryDAO;
import me.thereisnospoon.sparsam.dao.UserDAO;
import me.thereisnospoon.sparsam.services.entities.UserService;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import me.thereisnospoon.sparsam.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private ExpenseEntryDAO expenseEntryDAO;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void createUser(User user) {

		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setEncodedPassword(encodedPassword);
		userDAO.create(user);
	}

	@Override
	public User getUserByUsername(String username) {
		return userDAO.getUserByUsername(username);
	}

	@Override
	public ExpenseEntry addExpenseEntryForUser(Expense expense, String username) {

		ExpenseEntry expenseEntry = new ExpenseEntry();

		ExpenseCompositeKey expenseCompositeKey = new ExpenseCompositeKey();
		expenseCompositeKey.setUsername(username);
		expenseCompositeKey.setUniqueKey(generateUniqueKey());

		expenseEntry.setExpense(expense);
		expenseEntry.setExpenseCompositeKey(expenseCompositeKey);

		expenseEntryDAO.create(expenseEntry);
		return expenseEntry;
	}

	private String generateUniqueKey() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void updateExpenseEntryForUser(ExpenseCompositeKey expenseCompositeKey, Expense updatedExpense) {

		ExpenseEntry expenseEntry = new ExpenseEntry();
		expenseEntry.setExpenseCompositeKey(expenseCompositeKey);
		expenseEntry.setExpense(updatedExpense);

		expenseEntryDAO.update(expenseEntry);
	}
}
