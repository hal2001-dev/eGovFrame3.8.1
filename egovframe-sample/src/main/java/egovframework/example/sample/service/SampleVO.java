package egovframework.example.sample.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Sample data VO (one board row).
 */
@JsonIgnoreProperties({ "searchCondition", "searchKeyword", "pageIndex", "pageUnit",
		"pageSize", "firstIndex", "lastIndex", "recordCountPerPage" })
public class SampleVO extends SampleDefaultVO {

	private static final long serialVersionUID = 1L;

	/** primary key */
	private String id;

	/** name */
	private String name;

	/** description */
	private String description;

	/** use flag (Y/N) */
	private String useYn;

	/** register user */
	private String regUser;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}
