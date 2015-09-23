package me.thereisnospoon.sparsam.services.search.searchexecution;

import me.thereisnospoon.sparsam.services.search.indexing.ExpenseEntryFieldsForIndexing;
import me.thereisnospoon.sparsam.services.search.searchexecution.facets.Facet;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.Collection;

public interface ExpenseEntrySearcher {

	class Page {

		private Integer recordsPerPage;
		private Integer pageNumber;
		private Sort sort;

		public Page(Integer recordsPerPage, Integer pageNumber, Sort sort) {
			this.recordsPerPage = recordsPerPage;
			this.pageNumber = pageNumber;
			this.sort = sort;
		}

		public Integer getRecordsPerPage() {
			return recordsPerPage;
		}

		public Integer getPageNumber() {
			return pageNumber;
		}

		public Sort getSort() {
			return sort;
		}
	}

	SearchResult<ExpenseCompositeKey> search(String username, Collection<Facet> facets, Page page);

	static Sort getSortByDateOfExpense() {

		String dateOfExpenseFieldNameInIndex = ExpenseEntryFieldsForIndexing.DATE_OF_EXPENSE.getFieldNameInIndex();
		return new Sort(new SortField(dateOfExpenseFieldNameInIndex, SortField.Type.LONG, true));
	}
}
