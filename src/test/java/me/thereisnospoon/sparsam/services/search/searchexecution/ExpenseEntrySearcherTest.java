package me.thereisnospoon.sparsam.services.search.searchexecution;

import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryIndexer;
import me.thereisnospoon.sparsam.services.search.searchexecution.facets.AmountRangeFacet;
import me.thereisnospoon.sparsam.services.search.searchexecution.facets.DateRangeFacet;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.junit.After;
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

	private static final Double SMALLEST_AMOUNT = 10.;
	private static final Double MIDDLE_AMOUNT = 20.;
	private static final Double BIGGEST_AMOUNT = 30.;

	private static final String DESCRIPTION1 = "Test description for first expense entry";
	private static final String DESCRIPTION2 = "Test description which contains words which diverse from previous description";
	private static final String DESCRIPTION3 = "Testing text for third entry with some additional terms which describe";

	@Autowired
	private ExpenseEntrySearcher expenseEntrySearcher;

	@Autowired
	private ExpenseEntryIndexer expenseEntryIndexer;

	private List<String> entriesKeysOrderedByDate;
	private List<ExpenseEntry> testExpenseEntries = new LinkedList<>();

	@Before
	public void setUp() {

		testExpenseEntries = new LinkedList<>();
		testExpenseEntries.add(createExpenseEntry(TEST_USER1, LocalDate.now().plusDays(1), SMALLEST_AMOUNT, DESCRIPTION1));
		testExpenseEntries.add(createExpenseEntry(TEST_USER1, LocalDate.now(), MIDDLE_AMOUNT, DESCRIPTION2));
		testExpenseEntries.add(createExpenseEntry(TEST_USER1, LocalDate.now().minusDays(1), BIGGEST_AMOUNT, DESCRIPTION3));
		testExpenseEntries.add(createExpenseEntry(TEST_USER2, LocalDate.now(), MIDDLE_AMOUNT, DESCRIPTION1));

		entriesKeysOrderedByDate = testExpenseEntries.stream()
				.map(expenseEntry -> expenseEntry.getExpenseCompositeKey().getUniqueKey())
				.collect(Collectors.toList());

		testExpenseEntries.stream().forEach(expenseEntryIndexer::addExpenseEntryToIndex);
	}

	@After
	public void cleanUp() {
		testExpenseEntries.stream()
				.forEach(e -> expenseEntryIndexer.deleteExpenseEntryFromIndex(e.getExpenseCompositeKey()));
	}

	private ExpenseEntry createExpenseEntry(String username, LocalDate dateOfExpense, Double amount, String description) {

		Expense expense = new Expense();
		expense.setAmount(amount);
		expense.setDescription(description);
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
	public void testSearchResultCheckSortingOrder() {

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.emptyList(),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		List<ExpenseCompositeKey> foundRecords = searchResult.getFoundRecords();
		for (int i = 0; i < foundRecords.size(); i++) {

			String expectedEntryKeyIfOrderedByDate = entriesKeysOrderedByDate.get(i);
			String actualEntryKey = foundRecords.get(i).getUniqueKey();

			assertEquals(expectedEntryKeyIfOrderedByDate, actualEntryKey);
		}
	}

	private Set<String> getFoundKeys(SearchResult<ExpenseCompositeKey> searchResult) {
		return searchResult.getFoundRecords().stream().map(ExpenseCompositeKey::getUniqueKey).collect(Collectors.toSet());
	}

	@Test
	public void testFindEntriesStartingFromToday() {

		DateRangeFacet dateRangeFacet = new DateRangeFacet.Builder().setStartDate(LocalDate.now()).build();

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(dateRangeFacet),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		assertEquals(2, searchResult.getTotalHits().intValue());

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(0)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
	}

	@Test
	public void testFindEntriesTillToday() {

		DateRangeFacet dateRangeFacet = new DateRangeFacet.Builder().setEndDate(LocalDate.now()).build();

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(dateRangeFacet),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));
		assertEquals(2, foundKeys.size());
	}

	@Test
	public void testFindEntriesForYesterdayAndToday() {

		DateRangeFacet dateRangeFacet = new DateRangeFacet.Builder()
				.setStartDate(LocalDate.now().minusDays(1))
				.setEndDate(LocalDate.now())
				.build();

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(dateRangeFacet),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
		assertEquals(2, foundKeys.size());
	}

	@Test
	public void testFindEntriesWithAmountGreaterThanSmall() {

		AmountRangeFacet amountRangeFacet = new AmountRangeFacet.Builder().setLowerAmountBound(MIDDLE_AMOUNT).build();
		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(amountRangeFacet),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertEquals(2, searchResult.getTotalHits().intValue());
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));
	}

	@Test
	public void testFindEntryForGivenLowerAndUpperBoundAmount() {

		AmountRangeFacet amountRangeFacet = new AmountRangeFacet.Builder()
				.setLowerAmountBound(15.)
				.setUpperAmountBound(25.)
				.build();

		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher.search(TEST_USER1, Collections.singleton(amountRangeFacet),
				new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense()));

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertEquals(1, searchResult.getTotalHits().intValue());
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
	}

	@Test
	public void testFindAllEntriesByCommonTermFromDescription() {

		ExpenseEntrySearcher.Page page = new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense());
		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher
				.searchByDescription(TEST_USER1, "test", page);

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertEquals(3, searchResult.getTotalHits().intValue());
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(0)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));
	}

	@Test
	public void testFindTwoOfEntriesByDescription() {

		ExpenseEntrySearcher.Page page = new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense());
		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher
				.searchByDescription(TEST_USER1, "entries", page);

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertEquals(2, searchResult.getTotalHits().intValue());
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(0)));
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));
	}

	@Test
	public void testFindSingleEntryByUniqueTerm() {

		ExpenseEntrySearcher.Page page = new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense());
		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher
				.searchByDescription(TEST_USER1, "diverse", page);

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertEquals(1, searchResult.getTotalHits().intValue());
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(1)));
	}

	@Test
	public void testFindEntryByMultiTermSearch() {

		ExpenseEntrySearcher.Page page = new ExpenseEntrySearcher.Page(10, 1, ExpenseEntrySearcher.getSortByDateOfExpense());
		SearchResult<ExpenseCompositeKey> searchResult = expenseEntrySearcher
				.searchByDescription(TEST_USER1, "third texts", page);

		Set<String> foundKeys = getFoundKeys(searchResult);

		assertEquals(1, searchResult.getTotalHits().intValue());
		assertTrue(foundKeys.contains(entriesKeysOrderedByDate.get(2)));
	}
}