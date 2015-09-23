package me.thereisnospoon.sparsam.services.search.indexing.impl;

import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing;
import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryIndexer;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
public class ExpenseEntryIndexerImpl implements ExpenseEntryIndexer {

	@Autowired
	private IndexWriter indexWriter;

	@Override
	public void addExpenseEntryToIndex(ExpenseEntry expenseEntry) {

		Document document = createLuceneDocumentForExpenseEntry(expenseEntry);
		try {
			indexWriter.addDocument(document);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Document createLuceneDocumentForExpenseEntry(ExpenseEntry expenseEntry) {

		Document document = new Document();
		Arrays.asList(ExpenseEntryFieldsForIndexing.values())
				.forEach(expenseEntryFieldsForIndexing ->
						document.add(expenseEntryFieldsForIndexing.getLuceneField(expenseEntry)));

		return document;
	}

	@Override
	public void updateIndexedExpenseEntry(ExpenseEntry expenseEntry) {

		Document document = createLuceneDocumentForExpenseEntry(expenseEntry);
		Term expenseEntryIdTerm = getTermForExpenseEntryUniqueKey(expenseEntry.getExpenseCompositeKey());

		try {
			indexWriter.updateDocument(expenseEntryIdTerm, document);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Term getTermForExpenseEntryUniqueKey(ExpenseCompositeKey expenseCompositeKey) {
		return new Term(ExpenseEntryFieldsForIndexing.UNIQUE_KEY.getFieldNameInIndex(),
				expenseCompositeKey.getUniqueKey());
	}

	@Override
	public void deleteExpenseEntryFromIndex(ExpenseCompositeKey expenseCompositeKey) {

		Term expenseEntryIdTerm = getTermForExpenseEntryUniqueKey(expenseCompositeKey);
		try {
			indexWriter.deleteDocuments(expenseEntryIdTerm);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
