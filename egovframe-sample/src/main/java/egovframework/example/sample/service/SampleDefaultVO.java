package egovframework.example.sample.service;

import java.io.Serializable;

/**
 * 목록 조회 시 사용하는 공통 검색/페이징 VO.
 *
 * <p>게시판 목록 화면과 REST 목록 API에서 검색 조건(검색 구분/검색어)과
 * 페이징 정보를 담아 서비스·매퍼로 전달하기 위한 값 객체입니다.
 * 실제 게시글 데이터를 담는 {@link SampleVO} 가 이 클래스를 상속합니다.</p>
 *
 * <p>{@link Serializable} 을 구현해 세션 저장·직렬화 상황에서도 안전하게 다룹니다.</p>
 */
public class SampleDefaultVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 검색 구분 (0: 이름으로 검색, 1: 설명으로 검색, 그 외/빈값: 전체 검색) */
	private String searchCondition = "";

	/** 검색어 (LIKE 검색에 사용) */
	private String searchKeyword = "";

	/** 현재 페이지 번호 (1부터 시작) */
	private int pageIndex = 1;

	/** 한 페이지에 보여줄 데이터 건수 */
	private int pageUnit = 10;

	/** 페이지 하단 페이지 네비게이션에 표시할 페이지 개수 */
	private int pageSize = 10;

	/** 조회 시작 행 인덱스 (LIMIT/OFFSET 계산용) */
	private int firstIndex = 0;

	/** 조회 마지막 행 인덱스 */
	private int lastIndex = 1;

	/** 페이지당 실제 조회 건수 */
	private int recordCountPerPage = 10;

	/** 검색 구분을 반환한다. */
	public String getSearchCondition() {
		return searchCondition;
	}

	/** 검색 구분을 설정한다. */
	public void setSearchCondition(String searchCondition) {
		this.searchCondition = searchCondition;
	}

	/** 검색어를 반환한다. */
	public String getSearchKeyword() {
		return searchKeyword;
	}

	/** 검색어를 설정한다. */
	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	/** 현재 페이지 번호를 반환한다. */
	public int getPageIndex() {
		return pageIndex;
	}

	/** 현재 페이지 번호를 설정한다. */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/** 한 페이지 데이터 건수를 반환한다. */
	public int getPageUnit() {
		return pageUnit;
	}

	/** 한 페이지 데이터 건수를 설정한다. */
	public void setPageUnit(int pageUnit) {
		this.pageUnit = pageUnit;
	}

	/** 페이지 네비게이션 크기를 반환한다. */
	public int getPageSize() {
		return pageSize;
	}

	/** 페이지 네비게이션 크기를 설정한다. */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/** 조회 시작 행 인덱스를 반환한다. */
	public int getFirstIndex() {
		return firstIndex;
	}

	/** 조회 시작 행 인덱스를 설정한다. */
	public void setFirstIndex(int firstIndex) {
		this.firstIndex = firstIndex;
	}

	/** 조회 마지막 행 인덱스를 반환한다. */
	public int getLastIndex() {
		return lastIndex;
	}

	/** 조회 마지막 행 인덱스를 설정한다. */
	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	/** 페이지당 실제 조회 건수를 반환한다. */
	public int getRecordCountPerPage() {
		return recordCountPerPage;
	}

	/** 페이지당 실제 조회 건수를 설정한다. */
	public void setRecordCountPerPage(int recordCountPerPage) {
		this.recordCountPerPage = recordCountPerPage;
	}
}
