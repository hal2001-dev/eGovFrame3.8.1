package egovframework.example.secondary.service.impl;

import java.util.List;

import egovframework.example.cmm.annotation.SecondaryMapper;
import egovframework.example.secondary.service.ProductVO;

/**
 * 두 번째 DB(sampledb2)용 MyBatis 매퍼.
 *
 * <p>{@link SecondaryMapper} 애노테이션으로 표시되어, context-datasource-secondary.xml 의
 * 스캐너(annotationClass=SecondaryMapper)가 잡아 <b>sqlSession2(dataSource2)</b> 에
 * 바인딩됩니다. 기본 DB 스캐너는 {@code @Mapper} 만 잡으므로, 두 스캐너가 같은
 * basePackage 를 스캔해도 애노테이션이 달라 서로 겹치지 않습니다.</p>
 */
@SecondaryMapper
public interface ProductMapper {

	/** 상품 목록 조회 (DB2) */
	List<ProductVO> selectProductList();

	/** 상품 총 건수 조회 (DB2) */
	int selectProductListTotCnt();
}
