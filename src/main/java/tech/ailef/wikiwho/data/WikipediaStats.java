package tech.ailef.wikiwho.data;

import com.google.gson.annotations.SerializedName;

public class WikipediaStats {
	@SerializedName("organization_count")
	private int numOrganizations;
	
	@SerializedName("page_count")
	private int numPages;
	
	@SerializedName("edit_count")
	private int numEdits;

	public WikipediaStats(int numOrganizations, int numPages, int numEdits) {
		this.numOrganizations = numOrganizations;
		this.numPages = numPages;
		this.numEdits = numEdits;
	}

	public int getNumOrganizations() {
		return numOrganizations;
	}

	public int getNumPages() {
		return numPages;
	}

	public int getNumEdits() {
		return numEdits;
	}

	public void setNumOrganizations(int numOrganizations) {
		this.numOrganizations = numOrganizations;
	}

	public void setNumPages(int numPages) {
		this.numPages = numPages;
	}

	public void setNumEdits(int numEdits) {
		this.numEdits = numEdits;
	}
	
}
