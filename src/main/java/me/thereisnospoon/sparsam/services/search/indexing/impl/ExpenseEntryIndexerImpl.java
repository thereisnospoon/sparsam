package me.thereisnospoon.sparsam.services.search.indexing.impl;

import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing;
import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryIndexer;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ExpenseEntryIndexerImpl implements ExpenseEntryIndexer {

	@Autowired
	private IndexWriter indexWriter;

	@Override
	public void addExpenseEntryToIndex(ExpenseEntry expenseEntry) {

		Document document = new Document();
		Arrays.asList(ExpenseEntryFieldsForIndexing.values()).forEach(expenseEntryFieldsForIndexing ->
				document.add(expenseEntryFieldsForIndexing.getLuceneField(expenseEntry)));
	}

	@Override
	public void updateIndexedExpenseEntry(ExpenseEntry expenseEntry) {

	}
}
