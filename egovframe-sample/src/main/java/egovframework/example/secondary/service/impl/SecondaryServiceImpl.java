package egovframework.example.secondary.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.example.secondary.service.ProductVO;
import egovframework.example.secondary.service.SecondaryService;

/**
 * 두 번째 DB(sampledb2) 업무 서비스 구현체.
 *
 * <p>{@link ProductMapper}(sqlSession2/dataSource2 에 바인딩)를 사용합니다.
 * 트랜잭션은 클래스 레벨 {@code @Transactional("txManager2")} 로 <b>두 번째 트랜잭션 매니저</b>
 * 를 지정합니다. 매퍼(@SecondaryMapper)와 마찬가지로, 이제 서비스도 <b>애노테이션으로</b>
 * DS 를 구분하므로 특정 패키지에 두지 않아도 됩니다(기본 DB 서비스와 섞여 있어도 무방).</p>
 */
@Service("secondaryService")
@Transactional("txManager2")
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
