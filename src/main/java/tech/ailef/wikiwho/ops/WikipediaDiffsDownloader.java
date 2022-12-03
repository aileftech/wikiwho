package tech.ailef.wikiwho.ops;

import java.io.IOException;

import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.client.model.Filters;

import tech.ailef.wikiwho.data.Organization;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.storage.MongoDatabases;
import tech.ailef.wikiwho.wikiapi.WikiDiffRequest;

public class WikipediaDiffsDownloader {
	
	private static final String EDITS_FILE = "/mnt/data/wikipedia-data/filtered_edits.tsv";
	
	public static void main(String[] args) throws IOException {
		String editsFile = EDITS_FILE;
		if (args.length > 0) {
			editsFile = args[0];
		}

		System.out.println("Downloading and processing diffs...");
		download(editsFile);
	}
	
	public static WikipediaDiff processDiff(WikipediaDiff diff) {
		Document parse = Jsoup.parse("<table>" + diff.getDiffHtml() + "</table>");
		
		Elements rows = parse.select("tr");
		
		String addedText = "", deletedText = "";
		for (Element row : rows) {
			Elements deletedLines = row.select(".diff-deletedline");
			Elements deletions = deletedLines.select("del");
			
			Elements addedLines = row.select(".diff-addedline");
			Elements additions = addedLines.select("ins");
			
			if (deletions.isEmpty() && additions.isEmpty()) {
				deletedText += " " + deletedLines.text() + " ";
			} else {
				deletedText += " " + deletions.text() + " ";
			}
			
			if (additions.isEmpty() && deletions.isEmpty()) {
				addedText += " " + addedLines.text() + " ";
			} else {
				addedText += " " + additions.text() + " ";
			}
		}
		
		String[] parts = diff.getTimestamp().split("T");
		String date = parts[0];
		String[] dateParts = date.split("-");
		String year = dateParts[0];
		String month = dateParts[1];
		
		String monthYear = Integer.parseInt(month) + "-" + year;
		diff.setMonth(monthYear);
		
		diff.setTextAdded(addedText);
		diff.setTextRemoved(deletedText);
		
		OffsetDateTime dateTime = OffsetDateTime.parse(diff.getTimestamp());
		int intTimestamp = (int)(dateTime.toInstant().getEpochSecond());
		diff.setIntTimestamp(intTimestamp);
		
		return diff;
	}
	
	private static void download(String editsFile) throws IOException {
		System.out.println("Creating MongoDB indexes...");
		MongoDatabases.diffs.getMongoCollection().createIndex(new org.bson.Document("to_rev_id", 1));
		MongoDatabases.diffs.getMongoCollection().createIndex(new org.bson.Document("votes", -1));
		System.out.println("Done.");
		
		AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
		AtomicLong endTime = new AtomicLong(System.currentTimeMillis());
		AtomicInteger progress = new AtomicInteger(0);
		Files.lines(Paths.get(editsFile)).parallel().forEach(line -> {
			if (progress.incrementAndGet() % 1000 == 0) {
				endTime.set(System.currentTimeMillis());
				System.out.println("DiffDownloader\t" + progress.get() + " ("
						+ ((endTime.get() - startTime.get()) / 1000.0) + "s)");
				startTime.set(System.currentTimeMillis());
			}
			
			String[] parts = line.split("\t");
			
			if (parts.length != 4)
				return;
			
			String ip = parts[1];
			String org = parts[2];
			
			int revId = Integer.valueOf(parts[0]);
			
			WikipediaDiff findFirst = MongoDatabases.diffs.findFirst(Filters.eq("to_rev_id", (int)(revId)), new org.bson.Document(), new org.bson.Document());
			if (findFirst != null) {
				return;
			}
			
			try {
				WikipediaDiff diff = new WikiDiffRequest().getDiff(revId);
				
				if (diff != null) {
					diff.setIp(ip);
					diff.setOrganization(new Organization(org));
					processDiff(diff);
					MongoDatabases.diffs.upsertItem(diff);
				}
			} catch (SocketTimeoutException e) {
				System.out.println("Socket timeout...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {	}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
