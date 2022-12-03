package tech.ailef.wikiwho.ops;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.storage.LuceneBackedIndex;
import tech.ailef.wikiwho.storage.LuceneIndexFactory;
import tech.ailef.wikiwho.storage.MongoDatabases;

public class CreateLuceneIndex {
	public static void main(String[] args) {
		processFromMongo(args);
	}
	
	private static void processFromMongo(String[] args) {
		Iterator<WikipediaDiff> iterator = MongoDatabases.diffs.iterator();

		if (args.length == 0) {
			System.out.println("CreateLuceneIndex need Lucene index directory as first argument.");
			System.exit(0);
		}
		
		LuceneBackedIndex index = LuceneIndexFactory.getInstance(args[0]);
		
		long total = MongoDatabases.diffs.count();

		AtomicInteger progress = new AtomicInteger(0);
		while (iterator.hasNext()) {
			if (progress.incrementAndGet() % 100 == 0) {
				System.out.println("Progress: " + progress.get() + " / " + total);
			}

			WikipediaDiff next = iterator.next();
			WikipediaDiffsDownloader.processDiff(next);

			index.addDocument(next);
		}
	}
}
