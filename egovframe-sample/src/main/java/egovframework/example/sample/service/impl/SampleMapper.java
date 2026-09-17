package egovframework.example.sample.service.impl;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;

/**
 * 샘플(게시판) MyBatis 매퍼 인터페이스.
 *
 * <p>인터페이스에는 메서드 선언만 있고 구현체는 없습니다. 실제 구현은 MyBatis 가
 * 매퍼 XML(egovframework/mapper/example/sample_SQL_hsql.xml)의 SQL 을 읽어
 * 런타임에 동적으로 생성합니다. XML 의 {@code namespace} 는 이 인터페이스의
 * 완전한 클래스명(FQN)과 정확히 일치해야 하고, 각 SQL 의 id 는 메서드명과 같아야 합니다.</p>
 *
 * <p>{@link Mapper} 애노테이션이 붙어 있어 스프링 설정의 MapperScannerConfigurer 가
 * 이 인터페이스를 자동으로 스캔해 "sampleMapper" 라는 이름의 빈으로 등록합니다.</p>
 *
 * <p>패키지 위치({@code service.impl})는 eGovFrame 표준 샘플의 관례를 따른 것으로,
 * 위치와 무관하게 개념상 영속성(persistence) 계층입니다.</p>
 */
@Mapper
public interface SampleMapper {

	/** 검색 조건에 맞는 게시글 목록을 조회한다. */
	List<SampleVO> selectSampleList(SampleDefaultVO searchVO);

	/** 검색 조건에 맞는 게시글 총 건수를 조회한다. */
	int selectSampleListTotCnt(SampleDefaultVO searchVO);

	/** 단일 게시글을 조회한다. (없으면 null 반환) */
	SampleVO selectSample(String id);

	/** 게시글을 INSERT 한다. */
	void insertSample(SampleVO vo);

	/** 게시글을 UPDATE 한다. */
	void updateSample(SampleVO vo);

	/** 게시글을 DELETE 한다. */
	void deleteSample(String id);
}
