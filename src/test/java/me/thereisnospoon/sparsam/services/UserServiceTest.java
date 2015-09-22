package me.thereisnospoon.sparsam.services;

import me.thereisnospoon.sparsam.dao.ExpenseEntryDAO;
import me.thereisnospoon.sparsam.dao.UserDAO;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import me.thereisnospoon.sparsam.vo.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Currency;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/sparsam-config.xml")
public class UserServiceTest {

	private static final String TEST_USERNAME = "testUser3";
	private static final String TEST_RAW_PASSWORD = "123456";
	private static final Double TEST_AMOUNT = 10.;
	private static final Double TEST_UPDATED_AMOUNT = 20.;

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private ExpenseEntryDAO expenseEntryDAO;

	private User createNewTestUserIneMemory() {

		User user = new User();
		user.setUsername(TEST_USERNAME);
		user.setPassword(TEST_RAW_PASSWORD);
		return user;
	}

	private void createUserInRedis() {

		User newUser = createNewTestUserIneMemory();
		userService.createUser(newUser);
	}

	@Before
	public void setUp() {
		createUserInRedis();
	}

	@After
	public void cleanUp() {

		if (userDAO.exists(TEST_USERNAME)) {

			userDAO.delete(userDAO.getUserByUsername(TEST_USERNAME));
			deleteUsersExpenseEntries(TEST_USERNAME);
		}
	}

	private void deleteUsersExpenseEntries(String username) {
		expenseEntryDAO.getExpensesForUser(username).stream().forEach(expenseEntryDAO::delete);
	}

	@Test
	public void testCreateUser() throws Exception {

		User user = userService.getUserByUsername(TEST_USERNAME);
		assertNull(user.getPassword());
		assertTrue(passwordEncoder.matches(TEST_RAW_PASSWORD, user.getEncodedPassword()));
	}

	private Expense createTestExpense() {

		Expense expense = new Expense();
		expense.setAmount(TEST_AMOUNT);
		expense.setCurrency(Currency.getInstance("USD"));
		expense.setDescription("description");
		return expense;
	}

	@Test
	public void testAddExpenseEntryForUser() {

		Expense expense = createTestExpense();
		ExpenseEntry expenseEntry = userService.addExpenseEntryForUser(expense, TEST_USERNAME);

		ExpenseEntry expenseEntryFromDB = expenseEntryDAO
				.getExpenseEntryByUsernameAndKey(TEST_USERNAME, expenseEntry.getUniqueKey());

		assertEquals(TEST_AMOUNT, expenseEntryFromDB.getExpense().getAmount());
	}

	@Test
	public void testUpdateExpenseEntryForUser() {

		Expense expense = createTestExpense();
		ExpenseEntry expenseEntry = userService.addExpenseEntryForUser(expense, TEST_USERNAME);

		Expense updatedExpense = createTestExpense();
		updatedExpense.setAmount(TEST_UPDATED_AMOUNT);

		userService.updateExpenseEntryForUser(TEST_USERNAME, expenseEntry.getUniqueKey(), updatedExpense);

		ExpenseEntry expenseEntryFromDB = expenseEntryDAO
				.getExpenseEntryByUsernameAndKey(TEST_USERNAME, expenseEntry.getUniqueKey());

		assertEquals(TEST_UPDATED_AMOUNT, expenseEntryFromDB.getExpense().getAmount());
	}
}