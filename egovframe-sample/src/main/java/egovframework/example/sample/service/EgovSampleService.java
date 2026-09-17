package egovframework.example.sample.service;

import java.util.List;

/**
 * 샘플(게시판) 업무 서비스 인터페이스.
 *
 * <p>컨트롤러(웹 계층)와 영속성(매퍼) 계층 사이의 비즈니스 로직 진입점입니다.
 * 컨트롤러는 이 인터페이스에만 의존하고, 실제 구현은
 * {@link egovframework.example.sample.service.impl.EgovSampleServiceImpl} 이 담당합니다.</p>
 */
public interface EgovSampleService {

	/**
	 * 검색 조건에 맞는 게시글 목록을 조회한다.
	 *
	 * @param searchVO 검색 조건(검색 구분/검색어 등)
	 * @return 게시글 목록
	 */
	List<SampleVO> selectSampleList(SampleDefaultVO searchVO) throws Exception;

	/**
	 * 검색 조건에 맞는 게시글 총 건수를 조회한다. (페이징 계산용)
	 *
	 * @param searchVO 검색 조건
	 * @return 총 건수
	 */
	int selectSampleListTotCnt(SampleDefaultVO searchVO) throws Exception;

	/**
	 * 단일 게시글을 조회한다.
	 *
	 * @param id 게시글 기본키
	 * @return 게시글 (존재하지 않으면 구현체에서 예외 발생)
	 */
	SampleVO selectSample(String id) throws Exception;

	/**
	 * 게시글을 등록한다. ID가 비어 있으면 구현체에서 자동 생성한다.
	 *
	 * @param vo 등록할 게시글
	 * @return 생성된 게시글의 ID
	 */
	String insertSample(SampleVO vo) throws Exception;

	/**
	 * 게시글을 수정한다.
	 *
	 * @param vo 수정할 게시글(ID 포함)
	 */
	void updateSample(SampleVO vo) throws Exception;

	/**
	 * 게시글을 삭제한다.
	 *
	 * @param id 삭제할 게시글 기본키
	 */
	void deleteSample(String id) throws Exception;
}
