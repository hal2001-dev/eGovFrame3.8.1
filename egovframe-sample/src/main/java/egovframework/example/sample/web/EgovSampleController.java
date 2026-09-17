package egovframework.example.sample.web;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;

/**
 * 샘플(게시판) 화면(JSP) 컨트롤러.
 *
 * <p>브라우저에서 접근하는 게시판 CRUD 화면을 담당합니다. 모든 화면 URL 은 웹 전용
 * 접두사 <b>{@code /action/sample}</b> 아래에 둡니다. 이렇게 하면 웹 요청은
 * {@code /action/*}, REST 요청은 {@code /api/*} 로 경로가 분리되어, 필터를
 * (웹 전용 / REST 전용)으로 깔끔하게 나눠 적용할 수 있습니다.</p>
 *
 * <p>처리 결과는 JSP 뷰 이름을 반환해 InternalResourceViewResolver 가
 * {@code /WEB-INF/jsp/**.jsp} 로 forward 합니다. (JSON 을 반환하는 REST 는
 * {@link egovframework.example.sample.api.EgovSampleApiController} 가 담당.)</p>
 */
@Controller
@RequestMapping("/action/sample")
public class EgovSampleController {

	/** 샘플 업무 서비스 */
	@Resource(name = "egovSampleService")
	private EgovSampleService egovSampleService;

	/**
	 * 게시글 목록 화면.
	 *
	 * @param searchVO 검색 조건(폼/쿼리스트링에서 바인딩)
	 * @param model    뷰로 전달할 데이터(목록/총건수)
	 * @return 목록 JSP 뷰 이름
	 */
	@RequestMapping(value = "/egovSampleList.do")
	public String selectSampleList(@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		List<SampleVO> sampleList = egovSampleService.selectSampleList(searchVO);
		model.addAttribute("resultList", sampleList);
		model.addAttribute("resultCnt", egovSampleService.selectSampleListTotCnt(searchVO));
		return "sample/egovSampleList";
	}

	/** 게시글 등록 화면(빈 폼)을 보여준다. */
	@RequestMapping(value = "/addSample.do", method = org.springframework.web.bind.annotation.RequestMethod.GET)
	public String addSampleView(@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		model.addAttribute("sampleVO", new SampleVO());
		return "sample/egovSampleRegister";
	}

	/** 등록 폼 제출 처리 후 목록으로 리다이렉트한다. */
	@RequestMapping(value = "/addSample.do", method = org.springframework.web.bind.annotation.RequestMethod.POST)
	public String addSample(@ModelAttribute("sampleVO") SampleVO sampleVO) throws Exception {
		egovSampleService.insertSample(sampleVO);
		return "redirect:/action/sample/egovSampleList.do";
	}

	/**
	 * 게시글 수정 화면. 선택한 ID 의 데이터를 조회해 폼에 채운다.
	 *
	 * @param id 수정 대상 게시글 ID
	 */
	@RequestMapping(value = "/updateSampleView.do")
	public String updateSampleView(@RequestParam("selectedId") String id,
			@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		model.addAttribute("sampleVO", egovSampleService.selectSample(id));
		return "sample/egovSampleRegister";
	}

	/** 수정 폼 제출 처리 후 목록으로 리다이렉트한다. */
	@RequestMapping(value = "/updateSample.do")
	public String updateSample(@ModelAttribute("sampleVO") SampleVO sampleVO) throws Exception {
		egovSampleService.updateSample(sampleVO);
		return "redirect:/action/sample/egovSampleList.do";
	}

	/** 게시글을 삭제한 뒤 목록으로 리다이렉트한다. */
	@RequestMapping(value = "/deleteSample.do")
	public String deleteSample(@RequestParam("selectedId") String id) throws Exception {
		egovSampleService.deleteSample(id);
		return "redirect:/action/sample/egovSampleList.do";
	}
}
