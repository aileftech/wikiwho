package tech.ailef.wikiwho.storage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;

import tech.ailef.wikiwho.data.WikipediaDiff;

public class LuceneBackedIndex {
	private IndexSearcher searcher;
	
	private IndexWriter writer;
	
	private SearcherManager searchManager;
	
	private String directory;
	
	protected LuceneBackedIndex(String directory) {
		this.directory = directory;
		
		try {
			FSDirectory fsDirectory = new NIOFSDirectory(Paths.get(directory));
			IndexWriterConfig config = new IndexWriterConfig();
			writer = new IndexWriter(fsDirectory, config);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			searchManager = new SearcherManager(writer, new SearcherFactory());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addDocument(WikipediaDiff document) {
		putItem(convertDoc(document));
	}
	
	private WikipediaDiff parseDoc(Document doc) {
		String id = doc.get("id");
		
		WikipediaDiff item = MongoDatabases.diffs.getItem(id);
		return item;
	}
	
	private Document convertDoc(WikipediaDiff document) {
		Document doc = new Document();
		if (document.getTextAdded() != null) doc.add(new TextField("added_text", document.getTextAdded(), Field.Store.NO));
		if (document.getTextRemoved() != null) doc.add(new TextField("removed_text", document.getTextRemoved(), Field.Store.NO));
		doc.add(new StringField("id", document.getId(), Field.Store.YES));
		
		return doc;
	}

	public String getDirectory() {
		return directory;
	}

	

	public void putItem(Document doc) {
		try {
			writer.updateDocument(new Term("id", doc.get("id")), doc);
			writer.commit();
			searchManager.maybeRefresh();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public long deleteItem(String id) {
		TermQuery query = new TermQuery(new Term("id", id));
		
		try {
			long deleteDocuments = writer.deleteDocuments(query);
			writer.commit();
			searchManager.maybeRefresh();
			return deleteDocuments;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public WikipediaDiff getItem(String id) {
		TermQuery query = new TermQuery(new Term("id", id));
		
		TopDocs result;
		try {
			searcher = searchManager.acquire();
			
			result = searcher.search(query, 1);
			
			if (result.scoreDocs.length == 0)
				return null;
			
			ScoreDoc scoreDoc = result.scoreDocs[0];
			Document doc = searcher.doc(scoreDoc.doc);
			
			searchManager.release(searcher);
			
			return parseDoc(doc);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	private List<WikipediaDiff> executeQuery(Query query, int documentLimit) {
		Set<String> ids = new HashSet<>();
		try {
			searcher = searchManager.acquire();
			
			TopDocs result = searcher.search(query, documentLimit);

			IntStream.range(0, result.scoreDocs.length).parallel().forEach(i -> {
				try {
					ScoreDoc scoreDoc = result.scoreDocs[i];
					Document doc = searcher.doc(scoreDoc.doc);

					ids.add(doc.get("id"));
				} catch (IOException e) {
					
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
		
		return MongoDatabases.diffs.getItems(ids);
	}
	
	public List<WikipediaDiff> retrieve(String query, boolean addedText, boolean removedText, int documentLimit) {
		Builder queryBuilder = new BooleanQuery.Builder();

		StandardQueryParser qpHelper = new StandardQueryParser();
		qpHelper.setAnalyzer(new StandardAnalyzer());
		
		if (addedText) {
			try {
				Query added = qpHelper.parse(query, "added_text");
				queryBuilder.add(added, BooleanClause.Occur.SHOULD);
			} catch (QueryNodeException e) {
				
			}
		}
		
		if (removedText) {
			try {
				Query removed = qpHelper.parse(query, "removed_text");
				queryBuilder.add(removed, BooleanClause.Occur.SHOULD);
			} catch (QueryNodeException e) {
			}
		}
		
		BooleanQuery resultQuery = queryBuilder.build();
		return executeQuery(resultQuery, documentLimit);
	}
}
