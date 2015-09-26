package me.thereisnospoon.sparsam.services.search.searchexecution.impl;

import com.google.common.collect.Lists;
import me.thereisnospoon.sparsam.services.search.searchexecution.ExpenseEntrySearcher;
import me.thereisnospoon.sparsam.services.search.searchexecution.SearchResult;
import me.thereisnospoon.sparsam.services.search.searchexecution.facets.Facet;
import me.thereisnospoon.sparsam.services.search.searchexecution.IndexSearcherFactory;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing.*;

@Service
public class ExpenseEntrySearcherImpl implements ExpenseEntrySearcher {

	@Autowired
	private IndexSearcherFactory indexSearcherFactory;

	@Override
	public SearchResult<ExpenseCompositeKey> search(String username, Collection<Facet> facets, Page page) {

		Query searchQuery = createSearchQueryFromFacets(username, facets);
		return executeSearchAndProcessResults(username, searchQuery, page);
	}

	private Query createSearchQueryFromFacets(String username, Collection<Facet> facets) {

		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		facets.forEach(facet -> booleanQueryBuilder.add(facet.buildQuery(), BooleanClause.Occur.MUST));

		booleanQueryBuilder.add(queryExpenseEntriesForUser(username), BooleanClause.Occur.MUST);

		return booleanQueryBuilder.build();
	}

	private SearchResult<ExpenseCompositeKey> executeSearchAndProcessResults(String username, Query searchQuery, Page page) {

		IndexSearcher indexSearcher = indexSearcherFactory.getIndexSearcher();

		try {
			TopDocs topDocs = indexSearcher.search(searchQuery, getResultsLimit(page), page.getSort());
			List<ExpenseCompositeKey> foundExpenses = processSearchResult(username, topDocs, indexSearcher);
			List<ExpenseCompositeKey> expensesOnRequestedPage = filterOutExpensesFromAnotherPage(foundExpenses, page);

			return new SearchResult<>(expensesOnRequestedPage, topDocs.totalHits);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<ExpenseCompositeKey> processSearchResult(String username, TopDocs topDocs, IndexSearcher indexSearcher) {

		List<ScoreDoc> scoreDocs = Arrays.asList(topDocs.scoreDocs);

		return scoreDocs.parallelStream()
				.map(scoreDoc ->
						new ExpenseCompositeKey(getExpenseEntryKeyFromScoreDoc(indexSearcher, scoreDoc), username))

				.collect(Collectors.toList());
	}

	private Query queryExpenseEntriesForUser(String username) {
		return new TermQuery(new Term(USERNAME.getFieldNameInIndex(), username));
	}

	private Integer getResultsLimit(Page page) {
		return page.getPageNumber() * page.getRecordsPerPage();
	}

	private String getExpenseEntryKeyFromScoreDoc(IndexSearcher indexSearcher, ScoreDoc scoreDoc) {
		try {
			return indexSearcher.doc(scoreDoc.doc).get(UNIQUE_KEY.getFieldNameInIndex());
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

	@Override
	public SearchResult<ExpenseCompositeKey> searchByDescription(String username, String freeText, Page page) {

		List<String> searchTokens = safeTokenizeText(freeText);
		Query query = buildQueryByDescriptionForUser(username, searchTokens);
		return executeSearchAndProcessResults(username, query, page);
	}

	private List<String> safeTokenizeText(String text) {
		try {
			return tokenizeText(text);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> tokenizeText(String text) throws IOException {

		Analyzer analyzer = new EnglishAnalyzer();
		TokenStream tokenStream = analyzer.tokenStream(null, text);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

		List<String> tokens = new LinkedList<>();

		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			tokens.add(charTermAttribute.toString());
		}
		return tokens;
	}

	private Query buildQueryByDescriptionForUser(String username, List<String> searchTokens) {

		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		searchTokens.stream()
				.forEach(token ->
						booleanQueryBuilder.add(
								new TermQuery(
										new Term(DESCRIPTION.getFieldNameInIndex(), token)),
								BooleanClause.Occur.MUST));

		booleanQueryBuilder.add(new TermQuery(
				new Term(USERNAME.getFieldNameInIndex(), username)), BooleanClause.Occur.MUST);

		return booleanQueryBuilder.build();
	}
}
