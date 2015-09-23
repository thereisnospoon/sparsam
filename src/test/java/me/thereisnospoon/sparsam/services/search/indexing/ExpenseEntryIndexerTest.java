package me.thereisnospoon.sparsam.services.search.indexing;

import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.util.Currency;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/sparsam-config.xml")
public class ExpenseEntryIndexerTest {

	private static final String TEST_USERNAME = "testUsername4";
	private static final String TEST_USERNAME2 = "testUsername5";
	private static final String TEST_USERNAME3 = "testUsername6";
	private static final String TEST_USERNAME4 = "testUsername7";
	private static final Double AMOUNT = 10.;
	private static final Double AMOUNT2 = 20.;
	private static final String UNIQUE_KEY1 = "1";
	private static final String UNIQUE_KEY2 = "2";

	@Autowired
	private ExpenseEntryIndexer expenseEntryIndexer;

	@Autowired
	private IndexSearcherFactory indexSearcherFactory;

	private ExpenseEntry createExpenseEntry(String username, String uniqueKey, LocalDate dateOfExpense,
	                                        Double amount, String description) {

		ExpenseCompositeKey expenseCompositeKey = new ExpenseCompositeKey();
		expenseCompositeKey.setUniqueKey(uniqueKey);
		expenseCompositeKey.setUsername(username);

		Expense expense = new Expense();
		expense.setDateOfExpense(dateOfExpense);
		expense.setAmount(amount);
		expense.setDescription(description);
		expense.setCurrency(Currency.getInstance("USD"));

		ExpenseEntry expenseEntry = new ExpenseEntry();
		expenseEntry.setExpense(expense);
		expenseEntry.setExpenseCompositeKey(expenseCompositeKey);
		return expenseEntry;
	}

	@Test
	public void testAddExpenseEntryToIndex() throws Exception {

		ExpenseEntry expenseEntry = createExpenseEntry(TEST_USERNAME, UNIQUE_KEY1, LocalDate.now(), AMOUNT,
				"description for user");

		ExpenseEntry expenseEntry2 = createExpenseEntry(TEST_USERNAME2, UNIQUE_KEY1, LocalDate.now().plusDays(2), AMOUNT2,
				"text to analyzer");

		expenseEntryIndexer.addExpenseEntryToIndex(expenseEntry);
		expenseEntryIndexer.addExpenseEntryToIndex(expenseEntry2);

		IndexSearcher indexSearcher = indexSearcherFactory.getIndexSearcher();

		Term termForUser1 = new Term(ExpenseEntryFieldsForIndexing.USERNAME.getFieldNameInIndex(), TEST_USERNAME);
		Query query = new TermQuery(termForUser1);
		TopDocs topDocs = indexSearcher.search(query, 10);

		Term termForUser2 = new Term(ExpenseEntryFieldsForIndexing.USERNAME.getFieldNameInIndex(), TEST_USERNAME2);

		assertEquals(1, topDocs.totalHits);

		BooleanQuery booleanQuery = new BooleanQuery.Builder()
				.add(new TermQuery(termForUser1), BooleanClause.Occur.SHOULD)
				.add(new TermQuery(termForUser2), BooleanClause.Occur.SHOULD)
				.build();

		assertEquals(2, indexSearcher.search(booleanQuery, 10).totalHits);
	}

	@Test
	public void testUpdateExpenseEntryInIndex() throws Exception {


		ExpenseEntry expenseEntryToUpdate = createExpenseEntry(TEST_USERNAME3, UNIQUE_KEY2, LocalDate.now(), AMOUNT2,
				"new descriptions");

		expenseEntryIndexer.addExpenseEntryToIndex(expenseEntryToUpdate);
		IndexSearcher indexSearcher = indexSearcherFactory.getIndexSearcher();

		Term termForUser3Entries = new Term(ExpenseEntryFieldsForIndexing.USERNAME.getFieldNameInIndex(), TEST_USERNAME3);
		assertEquals(1, indexSearcher.search(new TermQuery(termForUser3Entries), 10).totalHits);

		expenseEntryToUpdate.getExpenseCompositeKey().setUsername(TEST_USERNAME4);
		expenseEntryIndexer.updateIndexedExpenseEntry(expenseEntryToUpdate);

		indexSearcher = indexSearcherFactory.getIndexSearcher();
		Term termForUser4Entries = new Term(ExpenseEntryFieldsForIndexing.USERNAME.getFieldNameInIndex(), TEST_USERNAME4);
		assertEquals(1, indexSearcher.search(new TermQuery(termForUser4Entries), 10).totalHits);
	}
}