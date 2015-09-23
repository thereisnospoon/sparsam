package me.thereisnospoon.sparsam.services.search.searchexecution;

import me.thereisnospoon.sparsam.services.search.searchexecution.facets.Facet;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;

import java.util.Collection;

public interface ExpenseEntrySearcher {

	class Page {

		private Integer recordsPerPage;
		private Integer pageNumber;

		public Page(Integer recordsPerPage, Integer pageNumber) {
			this.recordsPerPage = recordsPerPage;
			this.pageNumber = pageNumber;
		}

		public Integer getRecordsPerPage() {
			return recordsPerPage;
		}

		public Integer getPageNumber() {
			return pageNumber;
		}
	}

	SearchResult<ExpenseCompositeKey> search(String username, Collection<Facet> facets, Page page);
}
