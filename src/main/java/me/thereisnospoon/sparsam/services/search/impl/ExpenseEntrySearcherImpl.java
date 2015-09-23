package me.thereisnospoon.sparsam.services.search.impl;

import me.thereisnospoon.sparsam.services.search.ExpenseEntrySearcher;
import me.thereisnospoon.sparsam.services.search.facets.Facet;
import me.thereisnospoon.sparsam.services.search.indexing.IndexSearcherFactory;
import me.thereisnospoon.sparsam.vo.ExpenseCompositeKey;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

public class ExpenseEntrySearcherImpl implements ExpenseEntrySearcher {

	@Autowired
	private IndexSearcherFactory indexSearcherFactory;

	@Override
	public List<ExpenseCompositeKey> search(Collection<Facet> facets, Page page) {

		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		facets.forEach(facet -> booleanQueryBuilder.add(facet.buildQuery(), BooleanClause.Occur.MUST));

		//TODO
		indexSearcherFactory.getIndexSearcher().

		return null;
	}
}
