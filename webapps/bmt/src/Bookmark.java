import java.util.HashMap;
import java.util.Map;

/**
 * // TODO comment
 * 
 * @author Guillaume Halb, Benjamin Lebit, Ayutaya Rattanatray
 */
public class Bookmark {
	
	/**
	 * Bookmark ID
	 */
	private Long id = null;

	/**
	 * Bookmark title
	 */
	private String title;
	
	/**
	 * Bookmark description
	 */
	private String description;
	
	/**
	 * Bookmark link
	 */
	private String link;
	
	/**
	 * Bookmark's tags
	 */
	private Map<Long,Tag> tags;
	
	/**
	 * Creates a new bookmark
	 * 
	 * @param title
	 * 
	 * @param id
	 * 
	 * @param description
	 * 
	 * @param link
	 * 
	 */
	public Bookmark(Long id, String title, String description, String link) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.link = link;
		this.setTags(new HashMap<>());
	}

	/**
	 * Creates a new bookmark
	 * 
	 * @param title
	 *           title
	 */
	public Bookmark(String title,String description, String link) {
		super();
		this.id = null;
		this.link = link;
		this.description = description;
		this.title = title;
		this.setTags(new HashMap<>());
	}



	public Bookmark(String title, String description, String link, Map<Long, Tag> tags) {
		super();
		this.id = null;
		this.title = title;
		this.description = description;
		this.link = link;
		this.setTags(tags);
	}

	public Bookmark(Long id, String title, String description, String link, Map<Long, Tag> tags) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.link = link;
		this.setTags(tags);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Encodes the tag in JSON.
	 * 
	 * @return JSON representation of the tag
	 */
	public String toJson() {
		String json = "{";
		if (id != null)
			json += "\"id\":" + this.id;
		if (title != null) {
			if (json.length() > 1)
				json += ", ";
			json += "\"title\":\"" + this.title + "\"";
		}
		if (description != null){
			if (json.length() > 1)
				json += ", ";
			json += "\"description\":\"" + this.description + "\"";
		}
		if (link != null){
			if (json.length() > 1)
				json += ", ";
			json += "\"link\":\"" + this.link + "\"";
		}
		String jsonTags = "";
		if ( !getTags().isEmpty() ){
			System.out.println("not empty");
			for (Tag tag : getTags().values())
			{
			    jsonTags += tag.toJson() + ", ";
			}
			jsonTags = jsonTags.substring(0, jsonTags.length()- 2);
		}else{
			System.out.println("empty");
		}
		json += ",\"tags\":[" + jsonTags + "]}";
		return json;
	}

	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		Bookmark other = (Bookmark) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "title: " + this.title + ", id: " + this.id + ", description: " + this.description + ", link: " + this.link;
	}

	public Map<Long,Tag> getTags() {
		return tags;
	}

	public void setTags(Map<Long,Tag> tags) {
		this.tags = tags;
	}
}
