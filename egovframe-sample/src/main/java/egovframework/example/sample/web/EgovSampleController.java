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
 * Sample CRUD controller.
 */
@Controller
public class EgovSampleController {

	@Resource(name = "egovSampleService")
	private EgovSampleService egovSampleService;

	/** root -> list */
	@RequestMapping(value = "/")
	public String index() {
		return "redirect:/egovSampleList.do";
	}

	/** list */
	@RequestMapping(value = "/egovSampleList.do")
	public String selectSampleList(@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		List<SampleVO> sampleList = egovSampleService.selectSampleList(searchVO);
		model.addAttribute("resultList", sampleList);
		model.addAttribute("resultCnt", egovSampleService.selectSampleListTotCnt(searchVO));
		return "sample/egovSampleList";
	}

	/** register form */
	@RequestMapping(value = "/addSample.do", method = org.springframework.web.bind.annotation.RequestMethod.GET)
	public String addSampleView(@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		model.addAttribute("sampleVO", new SampleVO());
		return "sample/egovSampleRegister";
	}

	/** register submit */
	@RequestMapping(value = "/addSample.do", method = org.springframework.web.bind.annotation.RequestMethod.POST)
	public String addSample(@ModelAttribute("sampleVO") SampleVO sampleVO) throws Exception {
		egovSampleService.insertSample(sampleVO);
		return "redirect:/egovSampleList.do";
	}

	/** edit form */
	@RequestMapping(value = "/updateSampleView.do")
	public String updateSampleView(@RequestParam("selectedId") String id,
			@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		model.addAttribute("sampleVO", egovSampleService.selectSample(id));
		return "sample/egovSampleRegister";
	}

	/** update submit */
	@RequestMapping(value = "/updateSample.do")
	public String updateSample(@ModelAttribute("sampleVO") SampleVO sampleVO) throws Exception {
		egovSampleService.updateSample(sampleVO);
		return "redirect:/egovSampleList.do";
	}

	/** delete */
	@RequestMapping(value = "/deleteSample.do")
	public String deleteSample(@RequestParam("selectedId") String id) throws Exception {
		egovSampleService.deleteSample(id);
		return "redirect:/egovSampleList.do";
	}
}
