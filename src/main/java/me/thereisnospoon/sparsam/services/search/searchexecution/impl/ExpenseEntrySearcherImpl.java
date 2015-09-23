package me.thereisnospoon.sparsam.services.search.searchexecution.impl;

import com.google.common.collect.Lists;
import me.thereisnospoon.sparsam.services.search.searchexecution.ExpenseEntrySearcher;
import me.thereisnospoon.sparsam.services.search.searchexecution.SearchResult;
import me.thereisnospoon.sparsam.services.search.searchexecution.facets.Facet;
import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing;
import me.thereisnospoon.sparsam.services.search.indexing.IndexSearcherFactory;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseEntrySearcherImpl implements ExpenseEntrySearcher {

	@Autowired
	private IndexSearcherFactory indexSearcherFactory;

	@Override
	public SearchResult<ExpenseCompositeKey> search(String username, Collection<Facet> facets, Page page) {

		Query searchQuery = createSearchQuery(username, facets);
		return executeSearchAndProcessResults(username, searchQuery, page);
	}

	private Query createSearchQuery(String username, Collection<Facet> facets) {

		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		facets.forEach(facet -> booleanQueryBuilder.add(facet.buildQuery(), BooleanClause.Occur.MUST));

		booleanQueryBuilder.add(queryExpenseEntriesForUser(username), BooleanClause.Occur.MUST);

		return booleanQueryBuilder.build();
	}

	private SearchResult<ExpenseCompositeKey> executeSearchAndProcessResults(String username, Query searchQuery, Page page) {

		IndexSearcher indexSearcher = indexSearcherFactory.getIndexSearcher();

		try {
			TopDocs topDocs = indexSearcher.search(searchQuery, getResultsLimit(page), getReverseSortByDateOfExpense());
			List<ExpenseCompositeKey> foundExpenses = processSearchResult(username, topDocs, indexSearcher);
			List<ExpenseCompositeKey> expensesOnRequestedPage = filterOutExpensesFromAnotherPage(foundExpenses, page);

			return new SearchResult<>(expensesOnRequestedPage, topDocs.totalHits);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Sort getReverseSortByDateOfExpense() {

		SortField reverseDateSortField = new SortField(ExpenseEntryFieldsForIndexing.DATE_OF_EXPENSE.getFieldNameInIndex(),
				SortField.Type.LONG, true);

		return new Sort(reverseDateSortField);
	}

	private List<ExpenseCompositeKey> processSearchResult(String username, TopDocs topDocs, IndexSearcher indexSearcher) {

		List<ScoreDoc> scoreDocs = Arrays.asList(topDocs.scoreDocs);

		return scoreDocs.parallelStream()
				.map(scoreDoc ->
						new ExpenseCompositeKey(getExpenseEntryKeyFromScoreDoc(indexSearcher, scoreDoc), username))

				.collect(Collectors.toList());
	}

	private Query queryExpenseEntriesForUser(String username) {
		return new TermQuery(new Term(ExpenseEntryFieldsForIndexing.USERNAME.getFieldNameInIndex(), username));
	}

	private Integer getResultsLimit(Page page) {
		return page.getPageNumber() * page.getRecordsPerPage();
	}

	private String getExpenseEntryKeyFromScoreDoc(IndexSearcher indexSearcher, ScoreDoc scoreDoc) {
		try {
			return indexSearcher.doc(scoreDoc.doc).get(ExpenseEntryFieldsForIndexing.UNIQUE_KEY.getFieldNameInIndex());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<ExpenseCompositeKey> filterOutExpensesFromAnotherPage(List<ExpenseCompositeKey> expenseCompositeKeys,
	                                                                   Page page) {

		List<List<ExpenseCompositeKey>> expenseEntriesPages = Lists.partition(expenseCompositeKeys, page.getRecordsPerPage());
		if (expenseEntriesPages.size() >= page.getPageNumber()) {
			return expenseEntriesPages.get(page.getPageNumber() - 1);
		} else {
			return Collections.emptyList();
		}
	}
}
