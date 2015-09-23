package me.thereisnospoon.sparsam.services.search.searchexecution;

import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryIndexer;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/sparsam-config.xml")
public class ExpenseEntrySearcherTest {

	private static final String TEST_USER1 = "userForExpenseEntrySearcherTest1";
	private static final String TEST_USER2 = "userForExpenseEntrySearcherTest2";

	@Autowired
	private ExpenseEntrySearcher expenseEntrySearcher;

	@Autowired
	private ExpenseEntryIndexer expenseEntryIndexer;

	@Before
	public void setUp() {

		expenseEntryIndexer.addExpenseEntryToIndex(createExpenseEntry(TEST_USER1, LocalDate.now(), 10.));
		expenseEntryIndexer.addExpenseEntryToIndex(createExpenseEntry(TEST_USER1, LocalDate.now().minusDays(1), 10.));
		expenseEntryIndexer.addExpenseEntryToIndex(createExpenseEntry(TEST_USER1, LocalDate.now().plusDays(1), 10.));
		expenseEntryIndexer.addExpenseEntryToIndex(createExpenseEntry(TEST_USER2, LocalDate.now(), 10.));
	}

	private ExpenseEntry createExpenseEntry(String username, LocalDate dateOfExpense, Double amount) {

		Expense expense = new Expense();
		expense.setAmount(amount);
		expense.setDescription("description");
		expense.setCurrency(Currency.getInstance("USD"));
		expense.setDateOfExpense(dateOfExpense);

		ExpenseCompositeKey expenseCompositeKey = new ExpenseCompositeKey(UUID.randomUUID().toString(), username);

		ExpenseEntry expenseEntry = new ExpenseEntry();
		expenseEntry.setExpense(expense);
		expenseEntry.setExpenseCompositeKey(expenseCompositeKey);

		return expenseEntry;
	}

	@Test
	public void testFindAllEntriesForUser() {

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.emptyList(),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		assertEquals(3, searchResult.getTotalHits().intValue());
	}
}