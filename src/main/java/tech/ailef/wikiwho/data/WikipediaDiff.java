package tech.ailef.wikiwho.data;

import java.time.OffsetDateTime;

import com.google.gson.annotations.SerializedName;

import tech.ailef.wikiwho.storage.BasicJsonSerializer;
import tech.ailef.wikiwho.storage.JsonObjectSerializer;
import tech.ailef.wikiwho.storage.JsonStorable;
import tech.ailef.wikiwho.utils.Hashing;

public class WikipediaDiff implements JsonStorable<String> {
	private static final BasicJsonSerializer<WikipediaDiff> serializer =
		new BasicJsonSerializer<>(WikipediaDiff.class);
	
	private static final String[] MONTHS = {
		"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"	
	};
	
	@SerializedName("_id")
	private String id;
	
	@SerializedName("from_title")
	private String fromTitle;
	
	@SerializedName("to_title")
	private String toTitle;
	
	@SerializedName("from_rev_id")
	private Long fromRevId;
	
	@SerializedName("to_rev_id")
	private Long toRevId;
	
	@SerializedName("from_size")
	private Integer fromSize;
	
	@SerializedName("to_size")
	private Integer toSize;
	
	@SerializedName("from_id")
	private Integer fromId;
	
	@SerializedName("to_id")
	private Integer toId;
	
	@SerializedName("html")
	private String diffHtml;
	
	private String ip;

	private Organization organization;
	
	@SerializedName("date")	
	private String dateTime;
	
	private int timestamp;

	@SerializedName("text_added")
	private String textAdded;
	
	@SerializedName("text_removed")
	private String textRemoved;
	
	private String month;
	
	private Integer votes;
	
	public WikipediaDiff(String fromTitle, String toTitle, long fromRevId, Long toRevId, Integer fromSize, Integer toSize,
			String diffHtml, String ip, Organization organization, int fromId, int toId, String date) {
		this.fromTitle = fromTitle;
		this.toTitle = toTitle;
		this.fromRevId = fromRevId;
		this.toRevId = toRevId;
		this.fromSize = fromSize;
		this.toSize = toSize;
		this.diffHtml = diffHtml;
		this.ip = ip;
		this.organization = organization;
		this.fromId = fromId;
		this.toId = toId;
		this.dateTime = date;
		this.id = Hashing.md5Hash(diffHtml + date + ip);
		this.votes = 0;
		
		OffsetDateTime dateTime = OffsetDateTime.parse(date);
		int timestamp = (int)(dateTime.toInstant().getEpochSecond());
		this.timestamp = timestamp;
	}

	public String getFromTitle() {
		return fromTitle;
	}
	
	public String getTextAdded() {
		return textAdded;
	}
	
	public String getTextRemoved() {
		return textRemoved;
	}

	public void setMonth(String month) {
		this.month = month;
	}
	
	public String getToTitle() {
		return toTitle;
	}

	public Long getFromRevId() {
		return fromRevId;
	}

	public Long getToRevId() {
		return toRevId;
	}

	public Integer getFromSize() {
		return fromSize;
	}

	public Integer getToSize() {
		return toSize;
	}
	
	public void incrementVotes() {
		if (votes ==  null) votes = 0;
		votes++;
	}
	
	public String getId() {
		if (id == null)
			id = Hashing.md5Hash(diffHtml + dateTime + ip);
		return id;
	}

	public String getDiffHtml() {
		return diffHtml;
	}

	public Organization getOrganization() {
		return organization;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getVotes() {
		if (votes == null) votes = 0;
		return votes;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Integer getFromId() {
		return fromId;
	}
	
	public Integer getToId() {
		return toId;
	}
	
	public String getTimestamp() {
		return dateTime;
	}
	
	public String getWikipediaURL() {
		return "https://en.wikipedia.org/w/index.php?&type=revision&diff=" + getToRevId() + "&oldid=prev";
	}
	
	/**
	 * The total amounts of bytes added or removed by this edit
	 * @return
	 */
	public int getDiffSize() {
		return getToSize() - getFromSize();
	}
	
	public void setTextAdded(String textAdded) {
		this.textAdded = textAdded;
	}
	
	public void setTextRemoved(String textRemoved) {
		this.textRemoved = textRemoved;
	}
	
	@Override
	public String toString() {
		return "WikipediaDiff [fromTitle=" + fromTitle + ", toTitle=" + toTitle + ", fromRevId=" + fromRevId
				+ ", toRevId=" + toRevId + ", fromSize=" + fromSize + ", toSize=" + toSize + ", diffHtml=" + diffHtml
				+ "]";
	}
	
	public String getMonth() {
		return month;
	}
	
	public String getReadableDate() {
		String[] parts = dateTime.split("T");
		
		String date = parts[0];
		String time = parts[1].replaceAll(":[0-9][0-9]Z$", "");
		
		String[] dateParts = date.split("-");
		String year = dateParts[0];
		String month = dateParts[1];
		String day = dateParts[2];
		
		return day + " " + MONTHS[Integer.parseInt(month) - 1] + " " + year + " at " + time;
	}
	
	public String getHash() {
		return Hashing.md5Hash(toString());
	}
	
	public void setIntTimestamp(int intTimestamp) {
		this.timestamp = intTimestamp;
	}
	
	public int getIntTimestamp() {
		return timestamp;
	}

	@Override
	public String toJson() {
		return serializer.serialize(this);
	}

	@Override
	public JsonObjectSerializer<?> getSerializer() {
		return serializer;
	}
	
	
}
