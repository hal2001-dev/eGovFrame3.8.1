package egovframework.example.secondary.service;

import java.util.List;

/**
 * 두 번째 DB(sampledb2) 업무 서비스 인터페이스.
 */
public interface SecondaryService {

	/** 상품 목록 조회 (DB2) */
	List<ProductVO> selectProductList() throws Exception;

	/** 상품 총 건수 조회 (DB2) */
	int selectProductListTotCnt() throws Exception;
}
