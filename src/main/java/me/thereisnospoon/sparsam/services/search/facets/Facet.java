package me.thereisnospoon.sparsam.services.search.facets;

import org.apache.lucene.search.Query;

public interface Facet {

	Query buildQuery();
}
