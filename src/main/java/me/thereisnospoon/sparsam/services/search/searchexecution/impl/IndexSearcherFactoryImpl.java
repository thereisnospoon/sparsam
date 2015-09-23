package me.thereisnospoon.sparsam.services.search.searchexecution.impl;

import me.thereisnospoon.sparsam.services.search.searchexecution.IndexSearcherFactory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IndexSearcherFactoryImpl implements IndexSearcherFactory {

	@Autowired
	private IndexWriter indexWriter;

	private DirectoryReader currentIndexReader;
	private IndexSearcher currentIndexSearcher;

	@Override
	public synchronized IndexSearcher getIndexSearcher() {

		if (currentIndexReader == null) {
			initializeReaderAndSearcher();
		} else {
			recreateIndexReaderAndSearcherIfNeeded();
		}

		return currentIndexSearcher;
	}

	private void initializeReaderAndSearcher() {

		try {
			currentIndexReader = DirectoryReader.open(indexWriter, true);
			currentIndexSearcher = new IndexSearcher(currentIndexReader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void recreateIndexReaderAndSearcherIfNeeded() {

		try {
			DirectoryReader newIndexReader = DirectoryReader.openIfChanged(currentIndexReader, indexWriter, true);
			if (newIndexReader != currentIndexReader && null != newIndexReader) {

				currentIndexReader = newIndexReader;
				currentIndexSearcher = new IndexSearcher(newIndexReader);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
