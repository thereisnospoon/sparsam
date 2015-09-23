package me.thereisnospoon.sparsam.services.search;

import me.thereisnospoon.sparsam.services.search.facets.Facet;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;

import java.util.Collection;
import java.util.List;

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

	List<ExpenseCompositeKey> search(Collection<Facet> facets, Page page);
}
