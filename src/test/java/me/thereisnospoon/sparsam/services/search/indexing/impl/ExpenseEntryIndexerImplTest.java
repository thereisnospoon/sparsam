package me.thereisnospoon.sparsam.services.search.indexing.impl;

import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing;
import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryIndexer;
import me.thereisnospoon.sparsam.vo.Expense;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/sparsam-config.xml")
public class ExpenseEntryIndexerImplTest {

	private static final String TEST_USERNAME = "testUsername4";
	private static final Double AMOUNT = 10.;

	@Autowired
	private ExpenseEntryIndexer expenseEntryIndexer;

	@Autowired
	private IndexSearcher indexSearcher;

	private ExpenseEntry createExpenseEntry() {

		ExpenseCompositeKey expenseCompositeKey = new ExpenseCompositeKey();
		expenseCompositeKey.setUniqueKey(UUID.randomUUID().toString());
		expenseCompositeKey.setUsername(TEST_USERNAME);

		Expense expense = new Expense();
		expense.setDateOfExpense(LocalDate.now());
		expense.setAmount(AMOUNT);
		expense.setDescription("123");
		expense.setCurrency(Currency.getInstance("USD"));

		ExpenseEntry expenseEntry = new ExpenseEntry();
		expenseEntry.setExpense(expense);
		expenseEntry.setExpenseCompositeKey(expenseCompositeKey);
		return expenseEntry;
	}

	@Before
	public void setUp() {

		ExpenseEntry expenseEntry = createExpenseEntry();
		expenseEntryIndexer.addExpenseEntryToIndex(expenseEntry);
	}

	@Test
	public void testAddExpenseEntryToIndex() throws Exception {

		Term term = new Term(ExpenseEntryFieldsForIndexing.USERNAME.getFieldNameInIndex(), TEST_USERNAME);
		Query query = new TermQuery(term);
		TopDocs topDocs = indexSearcher.search(query, 10);

		assertEquals(1, topDocs.totalHits);
	}
}