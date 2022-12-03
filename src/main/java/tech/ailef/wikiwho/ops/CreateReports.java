package tech.ailef.wikiwho.ops;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import tech.ailef.wikiwho.data.OrganizationReport;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.data.WikipediaPage;
import tech.ailef.wikiwho.data.WikipediaPageReport;
import tech.ailef.wikiwho.data.WikipediaReport;
import tech.ailef.wikiwho.data.OrganizationReport.Builder;
import tech.ailef.wikiwho.storage.MongoDatabases;

public class CreateReports {
	public static void main(String[] args) {
		Map<String, OrganizationReport.Builder> reports = new HashMap<>();
		tech.ailef.wikiwho.data.WikipediaReport.Builder rBuilder = new WikipediaReport.Builder("en");
		Map<WikipediaPage, WikipediaPageReport.Builder> pagesReport = new HashMap<>();
		
		Iterator<WikipediaDiff> it = MongoDatabases.diffs.iterator();
		AtomicInteger progress = new AtomicInteger(0);
		while (it.hasNext()) {
			if (progress.incrementAndGet() % 1000 == 0) {
				System.out.println("Progress: " + progress.get());
			}
			WikipediaDiff diff = it.next();

			if (diff.getToRevId() ==  null) continue;
			
			WikipediaPage page = new WikipediaPage(diff.getToTitle(), diff.getToId());

			reports.putIfAbsent(diff.getOrganization().getId(), new OrganizationReport.Builder(diff.getOrganization()));
			pagesReport.putIfAbsent(page, new WikipediaPageReport.Builder(page));
			
			Builder orgReportBuilder = reports.get(diff.getOrganization().getId());
			
			orgReportBuilder.addIp(diff.getIp());
			orgReportBuilder.incrementEditCount();
			orgReportBuilder.incrementTimestamp(diff);
			orgReportBuilder.incrementPageEdit(page);
			
			rBuilder.incrementEditCount();
			rBuilder.incrementOrganization(diff.getOrganization());
			rBuilder.incrementTimestamp(diff);
			rBuilder.incrementPageEdit(page);
			
			pagesReport.get(page).incrementEditCount();
			pagesReport.get(page).addIp(diff.getIp());
			pagesReport.get(page).incrementOrganization(diff.getOrganization());
			pagesReport.get(page).incrementTimestamp(diff);
		}
		
		WikipediaReport report3 = rBuilder.build();
		report3.cutOrganizations(1000);
		report3.cutPages(10000);
		MongoDatabases.wikipedias.upsertItem(report3);
		
		System.out.println("Generated " + pagesReport.size() + " pages reports, " + reports.size() + " organizations reports.");
		
		progress.set(0);
		pagesReport.forEach((page, builder) -> {
			if (progress.incrementAndGet() % 1000 == 0) {
				System.out.println("insertPagesReport: " + progress.get());
			}
			WikipediaPageReport report = builder.build();
			MongoDatabases.pages.upsertItem(report);
			pagesReport.put(page, null);
		});
		
		progress.set(0);
		reports.values().stream().forEach(builder -> {
			if (progress.incrementAndGet() % 1000 == 0) {
				System.out.println("insertOrganizationsReport: " + progress.get());
			}
			 

			OrganizationReport report = builder.build();
			MongoDatabases.organizationReports.upsertItem(report);
		});
	}
}
