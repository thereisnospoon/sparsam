package me.thereisnospoon.sparsam.services.search.indexing;

import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;

import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum ExpenseEntryFieldsForIndexing {

	USERNAME("username") {

		@Override
		public Field getLuceneField(ExpenseEntry expenseEntry) {

			ExpenseCompositeKey expenseCompositeKey = expenseEntry.getExpenseCompositeKey();
			return new StringField(getFieldNameInIndex(), expenseCompositeKey.getUsername(), Field.Store.NO);
		}
	},

	UNIQUE_KEY("key") {

		@Override
		public Field getLuceneField(ExpenseEntry expenseEntry) {

			ExpenseCompositeKey expenseCompositeKey = expenseEntry.getExpenseCompositeKey();
			return new StringField(getFieldNameInIndex(), expenseCompositeKey.getUniqueKey(), Field.Store.YES);
		}
	},

	DATE_OF_EXPENSE("dateOfExpense") {

		private Long getDateOfExpenseInMilliseconds(ExpenseEntry expenseEntry) {

			LocalDate dateOfExpense = expenseEntry.getExpense().getDateOfExpense();
			return getLocalDateAsLong(dateOfExpense);
		}

		@Override
		public Field getLuceneField(ExpenseEntry expenseEntry) {

			Long dateOfExpense = getDateOfExpenseInMilliseconds(expenseEntry);
			return new NumericDocValuesField(getFieldNameInIndex(), dateOfExpense);
		}
	},

	AMOUNT("amount") {

		@Override
		public Field getLuceneField(ExpenseEntry expenseEntry) {
			return new DoubleField(getFieldNameInIndex(), expenseEntry.getExpense().getAmount(), Field.Store.NO);
		}
	},

	CURRENCY("currency") {
		@Override
		public Field getLuceneField(ExpenseEntry expenseEntry) {
			return new StringField(getFieldNameInIndex(), expenseEntry.getExpense().getCurrency().getCurrencyCode(), Field.Store.NO);
		}
	},

	DESCRIPTION("description") {

		@Override
		public Field getLuceneField(ExpenseEntry expenseEntry) {
			return new TextField(getFieldNameInIndex(), expenseEntry.getExpense().getDescription(), Field.Store.NO);
		}

		@Override
		public Analyzer getFieldAnalyzer() {
			return new EnglishAnalyzer();
		}
	};

	private String fieldNameInIndex;

	ExpenseEntryFieldsForIndexing(String fieldNameInIndex) {

		this.fieldNameInIndex = fieldNameInIndex;
	}

	public static Long getLocalDateAsLong(LocalDate localDate) {

		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, LocalTime.MIN,
				ZoneId.systemDefault());

		return zonedDateTime.toInstant().getEpochSecond();
	}

	public static Map<String,Analyzer> getPerFieldAnalyzers() {

		List<ExpenseEntryFieldsForIndexing> fields = Arrays.asList(ExpenseEntryFieldsForIndexing.values());
		return fields.stream()
				.collect(Collectors.toMap(ExpenseEntryFieldsForIndexing::getFieldNameInIndex,
						ExpenseEntryFieldsForIndexing::getFieldAnalyzer));
	}

	public abstract Field getLuceneField(ExpenseEntry expenseEntry);

	public Analyzer getFieldAnalyzer() {
		return new StandardAnalyzer();
	}

	public String getFieldNameInIndex() {
		return this.fieldNameInIndex;
	}
}
