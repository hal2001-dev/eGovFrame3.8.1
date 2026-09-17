package egovframework.example.sample.web;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.secondary.service.SecondaryService;

/**
 * 두 개의 데이터소스를 동시에 사용하는 데모 컨트롤러.
 *
 * <p>한 요청에서 <b>기본 DB(dataSource, sampledb)</b> 의 게시글 건수와
 * <b>두 번째 DB(dataSource2, sampledb2)</b> 의 상품 목록을 함께 조회해 JSON 으로 반환합니다.
 * 각각 서로 다른 SqlSessionFactory/트랜잭션 매니저로 연결되어 있음을 보여줍니다.</p>
 *
 * <p>웹 경로({@code /action/multidb})라 HMAC 인증 없이 브라우저/curl 로 바로 확인할 수 있습니다.
 * ({@code @ResponseBody} 로 뷰 없이 JSON 직렬화)</p>
 */
@Controller
@RequestMapping("/action/multidb")
public class EgovMultiDbController {

	/** 기본 DB 서비스 (dataSource / sampledb) */
	@Resource(name = "egovSampleService")
	private EgovSampleService egovSampleService;

	/** 두 번째 DB 서비스 (dataSource2 / sampledb2) */
	@Resource(name = "secondaryService")
	private SecondaryService secondaryService;

	/** 두 DB 를 함께 조회해 결과를 JSON 으로 반환한다. */
	@GetMapping("/demo.do")
	@ResponseBody
	public Map<String, Object> demo() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();

		// DB1 (기본): 게시글 총 건수
		result.put("db1_sampleCount", egovSampleService.selectSampleListTotCnt(new SampleDefaultVO()));

		// DB2 (두 번째): 상품 총 건수 + 목록
		result.put("db2_productCount", secondaryService.selectProductListTotCnt());
		result.put("db2_productList", secondaryService.selectProductList());

		return result;
	}
}
