package me.thereisnospoon.sparsam.services.search.searchexecution;

import org.apache.lucene.search.IndexSearcher;

public interface IndexSearcherFactory {

	IndexSearcher getIndexSearcher();
}
