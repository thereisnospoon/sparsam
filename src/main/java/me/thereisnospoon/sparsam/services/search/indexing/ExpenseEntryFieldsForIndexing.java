package me.thereisnospoon.sparsam.services.search.indexing;

import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import me.thereisnospoon.sparsam.vo.ExpenseEntry;
import org.apache.lucene.document.*;

import java.time.*;

public enum ExpenseEntryFieldsForIndexing {

	USERNAME("username") {

		@Override
		public Field getField(ExpenseEntry expenseEntry) {

			ExpenseCompositeKey expenseCompositeKey = expenseEntry.getExpenseCompositeKey();
			return new StringField(this.fieldNameInIndex, expenseCompositeKey.getUsername(), Field.Store.NO);
		}
	},

	UNIQUE_KEY("key") {

		@Override
		public Field getField(ExpenseEntry expenseEntry) {

			ExpenseCompositeKey expenseCompositeKey = expenseEntry.getExpenseCompositeKey();
			return new StringField(this.fieldNameInIndex, expenseCompositeKey.getUniqueKey(), Field.Store.NO);
		}
	},

	DATE_OF_EXPENSE("dateOfExpense") {

		private Long getDateOfExpenseInMilliseconds(ExpenseEntry expenseEntry) {

			LocalDate dateOfExpense = expenseEntry.getExpense().getDateOfExpense();
			ZonedDateTime dateOfExpenseAsZonedDT = ZonedDateTime.of(dateOfExpense, LocalTime.now(),
					ZoneId.systemDefault());

			return dateOfExpenseAsZonedDT.toInstant().toEpochMilli();
		}

		@Override
		public Field getField(ExpenseEntry expenseEntry) {

			Long dateOfExpense = getDateOfExpenseInMilliseconds(expenseEntry);
			return new LongField(this.fieldNameInIndex, dateOfExpense, Field.Store.NO);
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
