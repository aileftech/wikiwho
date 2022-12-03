package tech.ailef.wikiwho.storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoManager {
	private static Map<MongoParameters, MongoManager> instances = new ConcurrentHashMap<>();

	private MongoClient client;
	
	public static synchronized MongoManager getInstance(MongoParameters params) {
		if (instances.get(params) == null)
			instances.put(params, new MongoManager(params));
		return instances.get(params);
	}
	
	private MongoManager(MongoParameters params) {
		if (params.getAuthDatabaseName() != null && params.getUsername() != null
			&& params.getPassword() != null) {
			
			client = MongoClients.create(MongoClientSettings.builder()
	                .applyToClusterSettings(builder -> 
                    	builder.hosts(Arrays.asList(new ServerAddress(params.getHost(), params.getPort()))))
            .credential(params.getMongoCredential())
            .build());
			
		} else {
			client =  MongoClients.create(MongoClientSettings.builder()
	                .applyToClusterSettings(builder -> 
                	builder.hosts(Arrays.asList(new ServerAddress(params.getHost(), params.getPort())))
                	)
        .build());
	                
		}
	}

	public void close() throws IOException {
		client.close();
	}
	
	public MongoClient getClient() {
		return client;
	}
	
	public MongoDatabase getDatabase(String databaseName) {
		return client.getDatabase(databaseName);
	}
}
