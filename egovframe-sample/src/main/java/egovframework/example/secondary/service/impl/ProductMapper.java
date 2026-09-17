package egovframework.example.secondary.service.impl;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import egovframework.example.secondary.service.ProductVO;

/**
 * 두 번째 DB(sampledb2)용 MyBatis 매퍼.
 *
 * <p>이 매퍼는 {@code egovframework.example.secondary} 패키지에 있으므로
 * context-datasource-secondary.xml 의 스캐너가 잡아 <b>sqlSession2(dataSource2)</b> 에
 * 바인딩됩니다. 기본 DB 스캐너(sample/cmm 만 스캔)와 겹치지 않습니다.</p>
 */
@Mapper
public interface ProductMapper {

	/** 상품 목록 조회 (DB2) */
	List<ProductVO> selectProductList();

	/** 상품 총 건수 조회 (DB2) */
	int selectProductListTotCnt();
}
