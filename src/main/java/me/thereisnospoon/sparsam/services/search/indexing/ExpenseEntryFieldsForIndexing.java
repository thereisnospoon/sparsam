package me.thereisnospoon.sparsam.services.search.indexing;

import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.apache.lucene.document.*;

public enum ExpenseEntryFieldsForIndexing {

	USERNAME("username") {

		@Override
		public Field getField(ExpenseEntry expenseEntry) {
			return new StringField(this.fieldNameInIndex, expenseEntry.getUsername(), Field.Store.NO);
		}
	},

	UNIQUE_KEY("key") {

		@Override
		public Field getField(ExpenseEntry expenseEntry) {
			return new StringField(this.fieldNameInIndex, expenseEntry.getUniqueKey(), Field.Store.NO);
		}
	},

	DATE_OF_EXPENSE("dateOfExpense") {

		@Override
		public Field getField(ExpenseEntry expenseEntry) {
			return new LongField(this.fieldNameInIndex, expenseEntry.getDateOfExpense().toEpochMilli(), Field.Store.NO);
		}
	},

	AMOUNT("amount") {

		@Override
		public Field getField(ExpenseEntry expenseEntry) {
			return new DoubleField(this.fieldNameInIndex, expenseEntry.getExpense().getAmount(), Field.Store.NO);
		}
	},

	CURRENCY("currency") {
		@Override
		public Field getField(ExpenseEntry expenseEntry) {
			return new StringField(this.fieldNameInIndex, expenseEntry.getExpense().getCurrency().getCurrencyCode(), Field.Store.NO);
		}
	},

	DESCRIPTION("description") {

		@Override
		public Field getField(ExpenseEntry expenseEntry) {
			return new TextField(this.fieldNameInIndex, expenseEntry.getExpense().getDescription(), Field.Store.NO);
		}
	};

	String fieldNameInIndex;

	ExpenseEntryFieldsForIndexing(String fieldNameInIndex) {

		this.fieldNameInIndex = fieldNameInIndex;
	}

	public abstract Field getField(ExpenseEntry expenseEntry);
}
