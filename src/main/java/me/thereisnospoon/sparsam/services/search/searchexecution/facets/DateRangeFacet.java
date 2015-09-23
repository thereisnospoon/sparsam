package me.thereisnospoon.sparsam.services.search.searchexecution.facets;

import com.google.common.base.Preconditions;
import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

import java.time.LocalDate;

import static me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing.*;

public class DateRangeFacet implements Facet {

	private LocalDate startDate;
	private LocalDate endDate;

	private DateRangeFacet() {
	}

	public static class Builder {

		private DateRangeFacet dateRangeFacet = new DateRangeFacet();

		public Builder setStartDate(LocalDate startDate) {

			Preconditions.checkNotNull(startDate);
			dateRangeFacet.startDate = startDate;
			return this;
		}

		public Builder setEndDate(LocalDate endDate) {

			Preconditions.checkNotNull(endDate);
			dateRangeFacet.endDate = endDate;
			return this;
		}

		public DateRangeFacet build() {

			if (dateRangeFacet.startDate == null && dateRangeFacet.endDate == null) {
				throw new IllegalStateException("At least one bound of DateRangeFacet should be specified");
			}
			return dateRangeFacet;
		}
	}

	@Override
	public Query buildQuery() {

		return NumericRangeQuery.newLongRange(ExpenseEntryFieldsForIndexing.DESCRIPTION.getFieldNameInIndex(),
						getLocalDateAsLong(startDate), getLocalDateAsLong(endDate),
						true, true);
	}
}
