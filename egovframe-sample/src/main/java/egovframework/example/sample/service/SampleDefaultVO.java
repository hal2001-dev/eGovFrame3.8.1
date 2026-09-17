package egovframework.example.sample.service;

import java.io.Serializable;

/**
 * Common search / paging VO for the sample.
 */
public class SampleDefaultVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/** search condition (0: name, 1: description) */
	private String searchCondition = "";

	/** search keyword */
	private String searchKeyword = "";

	/** current page index (1-based) */
	private int pageIndex = 1;

	/** rows per page */
	private int pageUnit = 10;

	/** page navigation size */
	private int pageSize = 10;

	/** first record index (for LIMIT/OFFSET) */
	private int firstIndex = 0;

	/** last record index */
	private int lastIndex = 1;

	/** record count per page */
	private int recordCountPerPage = 10;

	public String getSearchCondition() {
		return searchCondition;
	}

	public void setSearchCondition(String searchCondition) {
		this.searchCondition = searchCondition;
	}

	public String getSearchKeyword() {
		return searchKeyword;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageUnit() {
		return pageUnit;
	}

	public void setPageUnit(int pageUnit) {
		this.pageUnit = pageUnit;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public void setFirstIndex(int firstIndex) {
		this.firstIndex = firstIndex;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	public int getRecordCountPerPage() {
		return recordCountPerPage;
	}

	public void setRecordCountPerPage(int recordCountPerPage) {
		this.recordCountPerPage = recordCountPerPage;
	}
}
