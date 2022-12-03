package tech.ailef.wikiwho.storage;

import tech.ailef.wikiwho.data.OrganizationReport;

import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.data.WikipediaDiffVote;
import tech.ailef.wikiwho.data.WikipediaPageReport;
import tech.ailef.wikiwho.data.WikipediaReport;

/*
 * If you need to change MongoDB connection paratmers you will need to change
 * this file. Instead of calling MongoParameters.localhostNoAuth() use MongoParameters.fromParams()
 * and pass the connection info. 
 */
public class MongoDatabases {
	public static final MongoStorage<OrganizationReport, String> organizationReports = new MongoStorage<OrganizationReport, String>(
			MongoManager.getInstance(MongoParameters.localhostNoAuth()), 
			"wikiwho", 
			"organizations", 
			new BasicJsonSerializer<>(OrganizationReport.class)
	);
	
	public static final MongoStorage<WikipediaDiff, String> diffs = new MongoStorage<WikipediaDiff, String>(
			MongoManager.getInstance(MongoParameters.localhostNoAuth()), 
			"wikiwho", 
			"diffs", 
			new BasicJsonSerializer<>(WikipediaDiff.class)
	);
	
	public static final MongoStorage<WikipediaReport, String> wikipedias = new MongoStorage<WikipediaReport, String>(
			MongoManager.getInstance(MongoParameters.localhostNoAuth()), 
			"wikiwho", 
			"wikipedias", 
			new BasicJsonSerializer<>(WikipediaReport.class)
	);
	
	public static final MongoStorage<WikipediaPageReport, String> pages = new MongoStorage<WikipediaPageReport, String>(
			MongoManager.getInstance(MongoParameters.localhostNoAuth()), 
			"wikiwho", 
			"pages", 
			new BasicJsonSerializer<>(WikipediaPageReport.class)
	);
	
	public static final MongoStorage<WikipediaDiffVote, String> diffsVote = new MongoStorage<WikipediaDiffVote, String>(
			MongoManager.getInstance(MongoParameters.localhostNoAuth()), 
			"wikiwho", 
			"diffs_votes", 
			new BasicJsonSerializer<>(WikipediaDiffVote.class)
	);
}
