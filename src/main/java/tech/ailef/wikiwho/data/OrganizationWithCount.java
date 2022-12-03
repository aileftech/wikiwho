package tech.ailef.wikiwho.data;

public class OrganizationWithCount {
	private Organization organization;
	
	private int count;

	public OrganizationWithCount(Organization org, int count) {
		this.organization = org;
		this.count = count;
	}

	public Organization getOrganization() {
		return organization;
	}

	public int getCount() {
		return count;
	}
	
}
