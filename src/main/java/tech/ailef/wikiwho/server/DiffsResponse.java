package tech.ailef.wikiwho.server;

import java.util.List;

import tech.ailef.wikiwho.data.Organization;
import tech.ailef.wikiwho.data.WikipediaDiff;
import tech.ailef.wikiwho.data.WikipediaPage;

public class DiffsResponse {
	private static final String[] MONTHS = {
		"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"	
	};
	
	private List<WikipediaDiff> diffs;
	
	private WikipediaPage page;

	private Organization organization;
	
	private Pagination pagination;
	
	private String month;

	public DiffsResponse(List<WikipediaDiff> diffs, WikipediaPage page, Organization organization, String month, Pagination pagination) {
		this.diffs = diffs;
		this.page = page;
		this.organization = organization;
		this.month = month;
		this.pagination = pagination;
	}

	public List<WikipediaDiff> getDiffs() {
		return diffs;
	}

	public WikipediaPage getPage() {
		return page;
	}
	
	public Pagination getPagination() {
		return pagination;
	}

	public String getMonth() {
		return month;
	}
	
	public String getMonthReadable() {
		String[] parts = month.split("-");
		String month = parts[0];
		String year = parts[1];
		
		return MONTHS[Integer.parseInt(month) - 1] + " " + year;
	}
	
	public Organization getOrganization() {
		return organization;
	}
}