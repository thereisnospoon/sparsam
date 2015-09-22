package me.thereisnospoon.sparsam.dao;

import me.thereisnospoon.sparsam.exceptions.dao.EntityAlreadyExistsException;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
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
		ExpenseCompositeKey expenseCompositeKey = new ExpenseCompositeKey();
		expenseCompositeKey.setUniqueKey(testExpenseEntryKey);
		expenseCompositeKey.setUsername(TEST_USERNAME);

		Expense expense = new Expense();
		expense.setDateOfExpense(LocalDate.now());
		expense.setAmount(TEST_EXPENSE_AMOUNT);
		expense.setCurrency(Currency.getInstance("USD"));
		expense.setDescription("some description");

		expenseEntry.setExpense(expense);
		expenseEntry.setExpenseCompositeKey(expenseCompositeKey);

		return expenseEntry;
	}

	private void deleteTestExpenseEntryIfExists() {

		ExpenseEntry expenseEntry = createTestExpenseEntry();
		if (expenseEntryDAO.exists(expenseEntry.getExpenseCompositeKey())) {
			expenseEntryDAO.delete(expenseEntry.getExpenseCompositeKey());
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
				.getExpenseEntryByCompositeKey(expenseEntry.getExpenseCompositeKey());

		assertEquals(testExpenseEntryKey, retrievedExpenseEntry.getExpenseCompositeKey().getUniqueKey());
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
		expenseEntry2.getExpenseCompositeKey().setUniqueKey(keyForSecondEntry);

		expenseEntryDAO.create(expenseEntry1);
		expenseEntryDAO.create(expenseEntry2);

		List<ExpenseEntry> testUserExpenses = expenseEntryDAO.getExpensesForUser(TEST_USERNAME);
		assertEquals(2, testUserExpenses.size());

		expenseEntryDAO.delete(expenseEntry2.getExpenseCompositeKey());
		List<ExpenseEntry> testUserExpensesAfterDeletionOfOne = expenseEntryDAO.getExpensesForUser(TEST_USERNAME);
		assertEquals(1, testUserExpensesAfterDeletionOfOne.size());

		expenseEntryDAO.delete(expenseEntry1.getExpenseCompositeKey());
		expenseEntryDAO.delete(expenseEntry2.getExpenseCompositeKey());

		List<ExpenseEntry> testUserExpensesAfterAllDeleted = expenseEntryDAO.getExpensesForUser(TEST_USERNAME);
		assertEquals(0, testUserExpensesAfterAllDeleted.size());
	}

	@Test
	public void testExpenseEntryUpdate() {

		ExpenseEntry expenseEntry = createTestExpenseEntry();
		expenseEntryDAO.create(expenseEntry);

		ExpenseCompositeKey expenseCompositeKey = new ExpenseCompositeKey();
		expenseCompositeKey.setUniqueKey(testExpenseEntryKey);
		expenseCompositeKey.setUsername(TEST_USERNAME);

		ExpenseEntry expenseEntryRetrievedFromDB = expenseEntryDAO
				.getExpenseEntryByCompositeKey(expenseCompositeKey);

		assertEquals(TEST_EXPENSE_AMOUNT, expenseEntryRetrievedFromDB.getExpense().getAmount());

		Expense expenseToUpdate = expenseEntry.getExpense();
		expenseToUpdate.setAmount(TEST_UPDATED_EXPENSE_AMOUNT);

		expenseEntryDAO.update(expenseEntry);
		ExpenseEntry expenseEntryRetrievedFromDBAfterUpdate = expenseEntryDAO
				.getExpenseEntryByCompositeKey(expenseCompositeKey);

		assertEquals(TEST_UPDATED_EXPENSE_AMOUNT, expenseEntryRetrievedFromDBAfterUpdate.getExpense().getAmount());
	}
}