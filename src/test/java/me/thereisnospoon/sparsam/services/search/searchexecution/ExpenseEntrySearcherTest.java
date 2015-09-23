package me.thereisnospoon.sparsam.services.search.searchexecution;

import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryIndexer;
import me.thereisnospoon.sparsam.services.search.searchexecution.facets.DateRangeFacet;
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
import java.util.*;
import java.util.stream.Collectors;

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

	private List<String> entriesKeysOrderedByDate;

	@Before
	public void setUp() {

		List<ExpenseEntry> testExpenseEntries = new LinkedList<>();

		testExpenseEntries.add(createExpenseEntry(TEST_USER1, LocalDate.now().plusDays(1), 10.));
		testExpenseEntries.add(createExpenseEntry(TEST_USER1, LocalDate.now(), 10.));
		testExpenseEntries.add(createExpenseEntry(TEST_USER1, LocalDate.now().minusDays(1), 10.));
		testExpenseEntries.add(createExpenseEntry(TEST_USER2, LocalDate.now(), 10.));

		entriesKeysOrderedByDate = testExpenseEntries.stream()
				.map(expenseEntry -> expenseEntry.getExpenseCompositeKey().getUniqueKey())
				.collect(Collectors.toList());

		testExpenseEntries.stream().forEach(expenseEntryIndexer::addExpenseEntryToIndex);
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

		SearchResult<ExpenseCompositeKey> searchResult2 = expenseEntrySearcher.search(TEST_USER2, Collections.emptyList(),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		assertEquals(1, searchResult2.getTotalHits().intValue());
	}

	@Test
	public void testSearchResultCheckSoringOrder() {

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.emptyList(),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		List<ExpenseCompositeKey> foundRecords = searchResult.getFoundRecords();
		for (int i = 0; i < foundRecords.size(); i++) {

			String expectedEntryKeyIfOrderedByDate = entriesKeysOrderedByDate.get(i);
			String actualEntryKey = foundRecords.get(i).getUniqueKey();

			assertEquals(expectedEntryKeyIfOrderedByDate, actualEntryKey);
		}
	}

	@Test
	public void testFindEntriesInDateRange() {

		DateRangeFacet dateRangeFacet = new DateRangeFacet.Builder().setStartDate(LocalDate.now()).build();

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(dateRangeFacet),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		assertEquals(2, searchResult.getTotalHits().intValue());

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(0)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));

		DateRangeFacet dateRangeFacet2 = new DateRangeFacet.Builder().setEndDate(LocalDate.now()).build();

		searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(dateRangeFacet2),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		foundKeys = getFoundKeys(searchResult);

		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));

		DateRangeFacet dateRangeFacet3 = new DateRangeFacet.Builder()
				.setStartDate(LocalDate.now().minusDays(1))
				.setEndDate(LocalDate.now())
				.build();

		searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(dateRangeFacet3),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		foundKeys = getFoundKeys(searchResult);

		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
	}

	private Set<String> getFoundKeys(SearchResult<ExpenseCompositeKey> searchResult) {
		return searchResult.getFoundRecords().stream().map(ExpenseCompositeKey::getUniqueKey).collect(Collectors.toSet());
	}
}