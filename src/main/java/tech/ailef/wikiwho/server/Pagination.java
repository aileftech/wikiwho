package tech.ailef.wikiwho.server;

import com.google.gson.annotations.SerializedName;

public class Pagination {
	@SerializedName("current_page")
	private int currentPage;
	
	@SerializedName("max_page")
	private int maxPage;
	
	@SerializedName("page_size")
	private int pageSize;

	public Pagination(int currentPage, int maxPage, int pageSize) {
		this.currentPage = currentPage;
		this.maxPage = maxPage;
		this.pageSize = pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public int getPageSize() {
		return pageSize;
	}
	
	public int getRangeLow() {
		return Math.max(currentPage - 3, 0);
	}
	
	public int getRangeHigh() {
		return Math.min(currentPage + 3, maxPage - 1);
	}
}
