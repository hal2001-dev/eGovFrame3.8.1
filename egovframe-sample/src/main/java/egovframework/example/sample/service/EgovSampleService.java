package egovframework.example.sample.service;

import java.util.List;

/**
 * Sample business service.
 */
public interface EgovSampleService {

	List<SampleVO> selectSampleList(SampleDefaultVO searchVO) throws Exception;

	int selectSampleListTotCnt(SampleDefaultVO searchVO) throws Exception;

	SampleVO selectSample(String id) throws Exception;

	String insertSample(SampleVO vo) throws Exception;

	void updateSample(SampleVO vo) throws Exception;

	void deleteSample(String id) throws Exception;
}
