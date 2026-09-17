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
 * Sample business service implementation.
 */
@Service("egovSampleService")
public class EgovSampleServiceImpl implements EgovSampleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSampleServiceImpl.class);

	@Resource(name = "sampleMapper")
	private SampleMapper sampleMapper;

	@Override
	public List<SampleVO> selectSampleList(SampleDefaultVO searchVO) throws Exception {
		return sampleMapper.selectSampleList(searchVO);
	}

	@Override
	public int selectSampleListTotCnt(SampleDefaultVO searchVO) throws Exception {
		return sampleMapper.selectSampleListTotCnt(searchVO);
	}

	@Override
	public SampleVO selectSample(String id) throws Exception {
		SampleVO vo = sampleMapper.selectSample(id);
		if (vo == null) {
			throw new IllegalArgumentException("no such sample id: " + id);
		}
		return vo;
	}

	@Override
	public String insertSample(SampleVO vo) throws Exception {
		if (vo.getId() == null || vo.getId().trim().isEmpty()) {
			vo.setId(String.valueOf(System.currentTimeMillis()));
		}
		if (vo.getUseYn() == null || vo.getUseYn().trim().isEmpty()) {
			vo.setUseYn("Y");
		}
		LOGGER.debug("insertSample : {}", vo.getId());
		sampleMapper.insertSample(vo);
		return vo.getId();
	}

	@Override
	public void updateSample(SampleVO vo) throws Exception {
		sampleMapper.updateSample(vo);
	}

	@Override
	public void deleteSample(String id) throws Exception {
		sampleMapper.deleteSample(id);
	}
}
