package me.thereisnospoon.sparsam.services.search.searchexecution.facets;

import com.google.common.base.Preconditions;
import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

public class AmountRangeFacet implements Facet {

	private Double lowerAmountBound;
	private Double upperAmountBound;

	private AmountRangeFacet() {}

	public static class Builder {

		private AmountRangeFacet amountRangeFacet = new AmountRangeFacet();

		public Builder setLowerAmountBound(Double lowerAmountBound) {

			Preconditions.checkNotNull(lowerAmountBound);
			amountRangeFacet.lowerAmountBound = lowerAmountBound;
			return this;
		}

		public Builder setUpperAmountBound(Double upperAmountBound) {

			Preconditions.checkNotNull(upperAmountBound);
			amountRangeFacet.upperAmountBound = upperAmountBound;
			return this;
		}

		public AmountRangeFacet build() {

			if (amountRangeFacet.lowerAmountBound == null && amountRangeFacet.upperAmountBound == null) {
				throw new IllegalStateException("At least one bound of AmountRangeFacet should be specified");
			}

			if (amountRangeFacet.lowerAmountBound == null) {
				amountRangeFacet.lowerAmountBound = Double.MIN_VALUE;
			}

			if (amountRangeFacet.upperAmountBound == null) {
				amountRangeFacet.upperAmountBound = Double.MAX_VALUE;
			}

			return amountRangeFacet;
		}
	}

	@Override
	public Query buildQuery() {
		return NumericRangeQuery.newDoubleRange(ExpenseEntryFieldsForIndexing.AMOUNT.getFieldNameInIndex(),
				lowerAmountBound, upperAmountBound, true, true);
	}
}
