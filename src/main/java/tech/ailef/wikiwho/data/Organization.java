package tech.ailef.wikiwho.data;

import com.google.gson.annotations.SerializedName;

import tech.ailef.wikiwho.utils.Hashing;

public class Organization {
	private String name;
	
	private String id;
	
	private String country;
	
	@SerializedName("normalized_name")
	private String normalizedName;

	public Organization(String name) {
		this.name = name;
		this.normalizedName = name.toLowerCase();
		this.id = Hashing.md5Hash(name);
	}

	public String getName() {
		return name;
	}
	
	public String getId() {
		if (id == null)
			id = Hashing.md5Hash(name);
		return id;
	}

	public String getNormalizedName() {
		return normalizedName;
	}
	
	public String getEscapedName() {
		return getName().replace("'", "\\'");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Organization other = (Organization) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	
}
