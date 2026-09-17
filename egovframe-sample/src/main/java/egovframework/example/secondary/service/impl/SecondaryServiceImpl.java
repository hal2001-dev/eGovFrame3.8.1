package egovframework.example.secondary.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import egovframework.example.secondary.service.ProductVO;
import egovframework.example.secondary.service.SecondaryService;

/**
 * 두 번째 DB(sampledb2) 업무 서비스 구현체.
 *
 * <p>{@link ProductMapper}(sqlSession2/dataSource2 에 바인딩)를 사용합니다.
 * 트랜잭션은 context-datasource-secondary.xml 의 AOP 가 이 구현체
 * ({@code egovframework.example.secondary..service.impl.*Impl})에 <b>txManager2</b> 를
 * 적용합니다. 기본 DB 트랜잭션(txManager)과 분리됩니다.</p>
 */
@Service("secondaryService")
public class SecondaryServiceImpl implements SecondaryService {

	@Resource(name = "productMapper")
	private ProductMapper productMapper;

	@Override
	public List<ProductVO> selectProductList() throws Exception {
		return productMapper.selectProductList();
	}

	@Override
	public int selectProductListTotCnt() throws Exception {
		return productMapper.selectProductListTotCnt();
	}
}
