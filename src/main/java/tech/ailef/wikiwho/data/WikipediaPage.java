package tech.ailef.wikiwho.data;

import com.google.gson.annotations.SerializedName;

public class WikipediaPage {
	@SerializedName("name")
	private String title;
	
	@SerializedName("normalized_name")
	private String normalizedName;
	
	private Integer id;

	public WikipediaPage(String title, Integer id) {
		this.title = title;
		this.id = id;
		this.normalizedName = title.toLowerCase();
	}
	
	public String getTitle() {
		return title;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return title + "#" + id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public String getEscapedTitle() {
		return getTitle().replace("'", "\\'");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WikipediaPage other = (WikipediaPage) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	

	
	
}
