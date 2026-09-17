package egovframework.example.sample.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;

/**
 * 샘플(게시판) 업무 서비스 구현체.
 *
 * <p>{@link EgovSampleService} 를 구현하며, 실제 데이터 접근은
 * {@link SampleMapper}(MyBatis) 에 위임합니다. 트랜잭션은 스프링 설정
 * (context-transaction.xml)의 AOP 어드바이스가 {@code ..service.impl.*Impl}
 * 메서드에 자동 적용하므로, 이 클래스에서 별도 트랜잭션 코드는 필요하지 않습니다.</p>
 *
 * <p>{@code @Service("egovSampleService")} 로 스프링 빈으로 등록되며,
 * 컨트롤러에서 {@code @Resource(name="egovSampleService")} 로 주입받습니다.</p>
 */
@Service("egovSampleService")
public class EgovSampleServiceImpl implements EgovSampleService {

	/** 로거 (SLF4J → Log4j2 로 연결됨) */
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSampleServiceImpl.class);

	/** MyBatis 매퍼 (MapperScannerConfigurer 가 등록한 "sampleMapper" 빈 주입) */
	@Resource(name = "sampleMapper")
	private SampleMapper sampleMapper;

	/** {@inheritDoc} 검색 조건에 맞는 목록을 그대로 매퍼에 위임한다. */
	@Override
	public List<SampleVO> selectSampleList(SampleDefaultVO searchVO) throws Exception {
		return sampleMapper.selectSampleList(searchVO);
	}

	/** {@inheritDoc} 검색 조건에 맞는 총 건수를 조회한다. */
	@Override
	public int selectSampleListTotCnt(SampleDefaultVO searchVO) throws Exception {
		return sampleMapper.selectSampleListTotCnt(searchVO);
	}

	/**
	 * {@inheritDoc}
	 * 단일 게시글을 조회하되, 해당 ID 가 없으면 {@link IllegalArgumentException} 을 던진다.
	 * (REST 컨트롤러는 이 예외를 404 응답으로 변환한다.)
	 */
	@Override
	public SampleVO selectSample(String id) throws Exception {
		SampleVO vo = sampleMapper.selectSample(id);
		if (vo == null) {
			throw new IllegalArgumentException("no such sample id: " + id);
		}
		return vo;
	}

	/**
	 * {@inheritDoc}
	 * 신규 등록. ID 가 비어 있으면 현재 시각(밀리초)으로 자동 생성하고,
	 * 사용 여부가 비어 있으면 기본값 'Y' 로 채운다.
	 *
	 * @return 생성(또는 전달)된 게시글 ID
	 */
	@Override
	public String insertSample(SampleVO vo) throws Exception {
		if (vo.getId() == null || vo.getId().trim().isEmpty()) {
			// 데모용 간단 ID 채번. 운영에서는 eGovFrame IdGnrService 나 시퀀스 사용 권장.
			vo.setId(String.valueOf(System.currentTimeMillis()));
		}
		if (vo.getUseYn() == null || vo.getUseYn().trim().isEmpty()) {
			vo.setUseYn("Y");
		}
		LOGGER.debug("insertSample : {}", vo.getId());
		sampleMapper.insertSample(vo);
		return vo.getId();
	}

	/** {@inheritDoc} 게시글을 수정한다. */
	@Override
	public void updateSample(SampleVO vo) throws Exception {
		sampleMapper.updateSample(vo);
	}

	/** {@inheritDoc} 게시글을 삭제한다. */
	@Override
	public void deleteSample(String id) throws Exception {
		sampleMapper.deleteSample(id);
	}
}
