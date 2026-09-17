package egovframework.example.sample.api;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;

/**
 * REST API for the sample resource. Returns/consumes JSON.
 *
 * <pre>
 *  GET    /api/samples            list (optional ?searchCondition=&searchKeyword=)
 *  GET    /api/samples/{id}       read one
 *  POST   /api/samples            create
 *  PUT    /api/samples/{id}       update
 *  DELETE /api/samples/{id}       delete
 * </pre>
 */
@RestController
@RequestMapping("/api/samples")
public class EgovSampleApiController {

	@Resource(name = "egovSampleService")
	private EgovSampleService egovSampleService;

	/** list (with optional search) */
	@GetMapping
	public Map<String, Object> list(
			@RequestParam(value = "searchCondition", required = false, defaultValue = "") String searchCondition,
			@RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword)
			throws Exception {
		SampleDefaultVO searchVO = new SampleDefaultVO();
		searchVO.setSearchCondition(searchCondition);
		searchVO.setSearchKeyword(searchKeyword);

		List<SampleVO> list = egovSampleService.selectSampleList(searchVO);
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("totalCount", egovSampleService.selectSampleListTotCnt(searchVO));
		body.put("list", list);
		return body;
	}

	/** read one */
	@GetMapping("/{id}")
	public SampleVO get(@PathVariable("id") String id) throws Exception {
		return egovSampleService.selectSample(id);
	}

	/** create */
	@PostMapping
	public ResponseEntity<SampleVO> create(@RequestBody SampleVO sampleVO) throws Exception {
		String id = egovSampleService.insertSample(sampleVO);
		SampleVO created = egovSampleService.selectSample(id);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).body(created);
	}

	/** update */
	@PutMapping("/{id}")
	public SampleVO update(@PathVariable("id") String id, @RequestBody SampleVO sampleVO) throws Exception {
		egovSampleService.selectSample(id); // 404 if not found
		sampleVO.setId(id);
		egovSampleService.updateSample(sampleVO);
		return egovSampleService.selectSample(id);
	}

	/** delete */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") String id) throws Exception {
		egovSampleService.selectSample(id); // 404 if not found
		egovSampleService.deleteSample(id);
		return ResponseEntity.noContent().build();
	}

	/** unknown id -> 404 with a small JSON body */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException e) {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("status", 404);
		body.put("message", e.getMessage());
		return new ResponseEntity<Map<String, Object>>(body, HttpStatus.NOT_FOUND);
	}
}
