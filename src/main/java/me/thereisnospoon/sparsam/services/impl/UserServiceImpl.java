package me.thereisnospoon.sparsam.services.impl;

import me.thereisnospoon.sparsam.dao.ExpenseEntryDAO;
import me.thereisnospoon.sparsam.dao.UserDAO;
import me.thereisnospoon.sparsam.services.UserService;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import me.thereisnospoon.sparsam.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
		expenseEntry.setExpense(expense);
		expenseEntry.setUsername(username);
		expenseEntry.setDateOfExpense(LocalDateTime.now());
		expenseEntry.setUniqueKey(generateUniqueKey());

		expenseEntryDAO.create(expenseEntry);
		return expenseEntry;
	}

	private String generateUniqueKey() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void updateExpenseEntryForUser(String username, String expenseEntryKey, Expense updatedExpense) {

		ExpenseEntry expenseEntry = new ExpenseEntry();
		expenseEntry.setUsername(username);
		expenseEntry.setUniqueKey(expenseEntryKey);
		expenseEntry.setExpense(updatedExpense);

		expenseEntryDAO.update(expenseEntry);
	}
}
