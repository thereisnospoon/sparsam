package me.thereisnospoon.sparsam.services.search.searchexecution;

import java.util.List;

public class SearchResult<T> {

	private List<T> foundRecords;
	private Integer totalHits;

	public SearchResult(List<T> foundRecords, Integer totalHits) {
		this.foundRecords = foundRecords;
		this.totalHits = totalHits;
	}

	public List<T> getFoundRecords() {
		return foundRecords;
	}

	public Integer getTotalHits() {
		return totalHits;
	}
}
