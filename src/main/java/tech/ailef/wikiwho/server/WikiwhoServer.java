package tech.ailef.wikiwho.server;

import java.nio.file.Files;
import java.nio.file.Paths;

import spark.Service;
import spark.Spark;

public class WikiwhoServer {
	private static final WikiwhoServerConfiguration conf = WikiwhoServerConfiguration.getInstance();
	
	private static void setUpUnsecureServer() {
		Service http = Service.ignite();
		
		http.port(conf.getInt("server.port"));
		boolean https = conf.getBoolean("server.https");
		
		if (https) {
			http.before((req, res) -> {
				 final String url = req.url();
	             if (url.startsWith("http://")) {
	                 final String[] split = url.split("http://");
	                 res.redirect("https://" + split[1]);
	             }
			});
		} else {
			http.staticFiles.externalLocation("static/");
			setupRoutes(http);
		}
			
		// Using Route
		http.internalServerError((req, res) -> {
		    res.redirect("/");
		    Spark.halt();
		    return "";
		});
	}
	
	private static void setUpSecureServer() {
		Service http = Service.ignite();
		
		if (!Files.exists(Paths.get("certs/keystore.jks"))) {
			System.err.println("[WARNING] Cannot setup secure server because certificate file does not exist.");
			return;
		}
		
		http.secure("certs/keystore.jks", "TODO", null, null);
		
		http.staticFiles.externalLocation("static/");
		http.port(443);
		
		setupRoutes(http);
	}
	
	private static void setupRoutes(Service http)  {
		/*
		 * Search endpoints:
		 *  - /autocomplete   (GET): returns data for the search autocomplete, both for simple and complex search
		 *  - /search         (GET): returns the results of the search with the Research Engine
		 */
		http.get("/organization/:id", new OrganizationEndpoint());
		
		http.get("/diffs/:orgid", new DiffsEndpoint());
		http.get("/diffs/:orgid/:pagenumber", new DiffsEndpoint());
		
		http.get("/diff/:id", new DiffEndpoint());
		
		http.get("/page/:pageid", new PageEndpoint());
		http.get("/page/:pageid/diffs", new PageDiffsEndpoint());
		http.get("/page/:pageid/diffs/:pagenumber", new PageDiffsEndpoint());
		
		http.get("/search", new FullTextSearchEndpoint());
		http.get("/autocomplete", new AutocompleteEndpoint());
		
		http.post("/vote/:id", new VoteDiffEndpoint());
		
		http.get("/", new HomeEndpoint());
		
	}
	
	public static void main(String[] args) {
		System.out.println("\n* Setting up HTTP server");
		setUpUnsecureServer();
		
		if (conf.getBoolean("server.https")) {
			System.out.println("\n* Setting up HTTPS server");
			setUpSecureServer();
		}
	}
}
