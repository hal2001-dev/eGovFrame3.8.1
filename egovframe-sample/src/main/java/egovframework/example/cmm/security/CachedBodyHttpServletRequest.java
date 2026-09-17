package egovframework.example.cmm.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StreamUtils;

/**
 * 요청 본문(body)을 메모리에 캐싱해 <b>두 번 읽을 수 있게</b> 해주는 요청 래퍼.
 *
 * <p>HTTP 요청 본문 스트림은 원래 한 번만 읽을 수 있습니다. 그런데 HMAC 인증은
 * 본문 해시로 서명을 검증해야 하고(1차 읽기), 이후 스프링 컨트롤러도
 * {@code @RequestBody} 로 같은 본문을 다시 읽어야 합니다(2차 읽기).
 * 이 래퍼는 생성 시 본문을 통째로 바이트 배열에 담아두고, 이후
 * getInputStream()/getReader() 호출 때마다 캐시에서 새 스트림을 만들어 반환합니다.</p>
 *
 * <p>필터에서 원본 요청을 이 래퍼로 감싼 뒤 체인에 넘기면, 필터와 컨트롤러가
 * 동일한 본문을 각각 온전히 읽을 수 있습니다.</p>
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

	/** 캐싱된 원본 요청 본문 */
	private final byte[] cachedBody;

	/**
	 * 원본 요청의 본문을 모두 읽어 캐시에 저장한다.
	 *
	 * @param request 원본 요청
	 * @throws IOException 본문 읽기 실패 시
	 */
	public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
	}

	/** 캐싱된 본문 바이트를 반환한다. (서명 검증 시 본문 해시 계산에 사용) */
	public byte[] getCachedBody() {
		return cachedBody;
	}

	/** 캐시된 본문을 기반으로 하는 새 ServletInputStream 을 반환한다. */
	@Override
	public ServletInputStream getInputStream() {
		final ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
		return new ServletInputStream() {
			@Override
			public int read() {
				return bais.read();
			}

			@Override
			public boolean isFinished() {
				return bais.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// 동기(블로킹) 방식으로만 사용하므로 비동기 리스너는 구현하지 않음
			}
		};
	}

	/** 캐시된 본문을 UTF-8 로 읽는 새 BufferedReader 를 반환한다. */
	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
	}
}
