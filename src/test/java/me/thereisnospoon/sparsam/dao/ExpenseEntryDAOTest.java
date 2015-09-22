package me.thereisnospoon.sparsam.dao;

import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/sparsam-config.xml")
public class ExpenseEntryDAOTest {

	private static final String TEST_USERNAME = "testuser2";
	private static final Double TEST_EXPENSE_AMOUNT = 10.;
	private static final Double TEST_UPDATED_EXPENSE_AMOUNT = 10.;

	private static String testExpenseEntryKey;

	@Autowired
	private ExpenseEntryDAO expenseEntryDAO;

	@BeforeClass
	public static void generateKeyForExpenseEntry() {
		testExpenseEntryKey = UUID.randomUUID().toString();
	}

	private ExpenseEntry createTestExpenseEntry() {

		ExpenseEntry expenseEntry = new ExpenseEntry();
		expenseEntry.setUsername(TEST_USERNAME);

		Expense expense = new Expense();

		expenseEntry.setDateOfExpense(Instant.now());
		expenseEntry.setUniqueKey(testExpenseEntryKey);

		expense.setAmount(TEST_EXPENSE_AMOUNT);
		expense.setCurrency(Currency.getInstance("USD"));
		expense.setDescription("some description");

		expenseEntry.setExpense(expense);

		return expenseEntry;
	}

	private void deleteTestExpenseEntryIfExists() {

		ExpenseEntry expenseEntry = createTestExpenseEntry();
		if (expenseEntryDAO.exists(expenseEntry)) {
			expenseEntryDAO.delete(expenseEntry);
		}
	}

	@Before
	public void setUp() throws Exception {
		deleteTestExpenseEntryIfExists();
	}

	@After
	public void tearDown() throws Exception {
		deleteTestExpenseEntryIfExists();
	}

	@Test
	public void testCreationOfExpenseEntry() {

		ExpenseEntry expenseEntry = createTestExpenseEntry();
		expenseEntryDAO.create(expenseEntry);

		ExpenseEntry retrievedExpenseEntry = expenseEntryDAO
				.getExpenseEntryByUsernameAndKey(expenseEntry.getUsername(), expenseEntry.getUniqueKey());

		assertEquals(testExpenseEntryKey, retrievedExpenseEntry.getUniqueKey());
		assertEquals(TEST_EXPENSE_AMOUNT, retrievedExpenseEntry.getExpense().getAmount());
	}

	@Test(expected = EntityAlreadyExistsException.class)
	public void testCreationOfExistingExpenseEntry() {

		ExpenseEntry expenseEntry = createTestExpenseEntry();
		expenseEntryDAO.create(expenseEntry);
		expenseEntryDAO.create(expenseEntry);
	}

	@Test
	public void testCreationAndDeletionOfSeveralEntities() {

		ExpenseEntry expenseEntry1 = createTestExpenseEntry();
		ExpenseEntry expenseEntry2 = createTestExpenseEntry();
		String keyForSecondEntry = UUID.randomUUID().toString();
		expenseEntry2.setUniqueKey(keyForSecondEntry);

		expenseEntryDAO.create(expenseEntry1);
		expenseEntryDAO.create(expenseEntry2);

		List<ExpenseEntry> testUserExpenses = expenseEntryDAO.getExpensesForUser(TEST_USERNAME);
		assertEquals(2, testUserExpenses.size());

		expenseEntryDAO.delete(expenseEntry2);
		List<ExpenseEntry> testUserExpensesAfterDeletionOfOne = expenseEntryDAO.getExpensesForUser(TEST_USERNAME);
		assertEquals(1, testUserExpensesAfterDeletionOfOne.size());

		expenseEntryDAO.delete(expenseEntry1);
		expenseEntryDAO.delete(expenseEntry2);

		List<ExpenseEntry> testUserExpensesAfterAllDeleted = expenseEntryDAO.getExpensesForUser(TEST_USERNAME);
		assertEquals(0, testUserExpensesAfterAllDeleted.size());
	}

	@Test
	public void testExpenseEntryUpdate() {

		ExpenseEntry expenseEntry = createTestExpenseEntry();
		expenseEntryDAO.create(expenseEntry);

		ExpenseEntry expenseEntryRetrievedFromDB = expenseEntryDAO
				.getExpenseEntryByUsernameAndKey(TEST_USERNAME, testExpenseEntryKey);

		assertEquals(TEST_EXPENSE_AMOUNT, expenseEntryRetrievedFromDB.getExpense().getAmount());

		Expense expenseToUpdate = expenseEntry.getExpense();
		expenseToUpdate.setAmount(TEST_UPDATED_EXPENSE_AMOUNT);

		expenseEntryDAO.update(expenseEntry);
		ExpenseEntry expenseEntryRetrievedFromDBAfterUpdate = expenseEntryDAO
				.getExpenseEntryByUsernameAndKey(TEST_USERNAME, testExpenseEntryKey);

		assertEquals(TEST_UPDATED_EXPENSE_AMOUNT, expenseEntryRetrievedFromDBAfterUpdate.getExpense().getAmount());
	}
}