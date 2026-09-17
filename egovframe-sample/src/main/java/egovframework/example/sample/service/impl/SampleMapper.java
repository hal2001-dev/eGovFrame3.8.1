package egovframework.example.sample.service.impl;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;

/**
 * MyBatis mapper for the sample. Implementation is generated from
 * the mapper XML (egovframework/mapper/example/sample_SQL_hsql.xml).
 */
@Mapper
public interface SampleMapper {

	List<SampleVO> selectSampleList(SampleDefaultVO searchVO);

	int selectSampleListTotCnt(SampleDefaultVO searchVO);

	SampleVO selectSample(String id);

	void insertSample(SampleVO vo);

	void updateSample(SampleVO vo);

	void deleteSample(String id);
}
