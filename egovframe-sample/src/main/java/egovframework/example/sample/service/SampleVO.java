package egovframework.example.sample.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 게시글(샘플) 한 건을 표현하는 데이터 VO.
 *
 * <p>검색/페이징 필드가 정의된 {@link SampleDefaultVO} 를 상속하므로,
 * 화면 폼 바인딩 시 데이터 필드와 검색 필드를 함께 담을 수 있습니다.</p>
 *
 * <p>{@link JsonIgnoreProperties} 로 상속받은 검색/페이징 필드(searchCondition,
 * pageIndex 등)를 JSON 직렬화에서 제외합니다. REST 응답에는 실제 데이터
 * 필드(id, name, description, useYn, regUser)만 노출됩니다.
 * 이 애노테이션은 JSON 직렬화에만 영향을 주며 JSP 화면 로직에는 영향이 없습니다.</p>
 */
@JsonIgnoreProperties({ "searchCondition", "searchKeyword", "pageIndex", "pageUnit",
		"pageSize", "firstIndex", "lastIndex", "recordCountPerPage" })
public class SampleVO extends SampleDefaultVO {

	private static final long serialVersionUID = 1L;

	/** 기본키(ID). 신규 등록 시 서비스에서 자동 생성한다. */
	private String id;

	/** 이름(제목) */
	private String name;

	/** 설명(내용) */
	private String description;

	/** 사용 여부 (Y: 사용, N: 미사용) */
	private String useYn;

	/** 등록자 */
	private String regUser;

	/** 기본키를 반환한다. */
	public String getId() {
		return id;
	}

	/** 기본키를 설정한다. */
	public void setId(String id) {
		this.id = id;
	}

	/** 이름을 반환한다. */
	public String getName() {
		return name;
	}

	/** 이름을 설정한다. */
	public void setName(String name) {
		this.name = name;
	}

	/** 설명을 반환한다. */
	public String getDescription() {
		return description;
	}

	/** 설명을 설정한다. */
	public void setDescription(String description) {
		this.description = description;
	}

	/** 사용 여부를 반환한다. */
	public String getUseYn() {
		return useYn;
	}

	/** 사용 여부를 설정한다. */
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	/** 등록자를 반환한다. */
	public String getRegUser() {
		return regUser;
	}

	/** 등록자를 설정한다. */
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}
