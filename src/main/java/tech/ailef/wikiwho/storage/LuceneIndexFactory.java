package tech.ailef.wikiwho.storage;

import java.util.HashMap;
import java.util.Map;

public class LuceneIndexFactory {
	
	private static Map<String, LuceneBackedIndex> instances = new HashMap<>();
	
	public synchronized static LuceneBackedIndex getInstance(String directory) {
		if (!instances.containsKey(directory))
			instances.put(directory, new LuceneBackedIndex(directory));
			
		return instances.get(directory);
	}
}
