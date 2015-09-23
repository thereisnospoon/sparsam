package me.thereisnospoon.sparsam.services.search.indexing;

import org.apache.lucene.search.IndexSearcher;

public interface IndexSearcherFactory {

	IndexSearcher getIndexSearcher();
}
