package tech.ailef.wikiwho.data;

import com.google.gson.annotations.SerializedName;

public class WikipediaPageStats {
	private WikipediaPage page;
	
	@SerializedName("edit_count")
	private int editCount;
	
	public WikipediaPageStats(WikipediaPage page, int editCount) {
		this.page = page;
		this.editCount = editCount;
	}

	public WikipediaPage getPage() {
		return page;
	}
	
	public int getEditCount() {
		return editCount;
	}

	public String getTitle() {
		return page.getTitle();
	}

	@Override
	public String toString() {
		return "WikipediaPageStats [page=" + page + ", editCount=" + editCount + "]";
	}
	
	
}
