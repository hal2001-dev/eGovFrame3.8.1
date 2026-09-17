package egovframework.example.cmm.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 서버-투-서버 요청 서명을 위한 HMAC / SHA-256 유틸리티.
 *
 * <p>외부 라이브러리 없이 JDK 표준 API(java.security, javax.crypto)만 사용합니다.
 * HMAC 검증 필터({@link HmacAuthFilter})가 본문 해시 계산과 서명 재계산에 사용합니다.</p>
 */
public final class HmacUtil {

	/** 16진수 인코딩용 문자 테이블(소문자) */
	private static final char[] HEX = "0123456789abcdef".toCharArray();

	/** 인스턴스화 방지(정적 유틸리티 클래스) */
	private HmacUtil() {
	}

	/**
	 * 입력 바이트의 SHA-256 해시를 소문자 16진수 문자열로 반환한다.
	 * 요청 본문(body)의 무결성 확인에 사용한다. (null 은 빈 배열로 처리)
	 */
	public static String sha256Hex(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return toHex(md.digest(data == null ? new byte[0] : data));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 비밀키(secret)로 메시지의 HMAC-SHA256 을 계산해 소문자 16진수로 반환한다.
	 * 서명 대상 문자열(stringToSign)에 대해 서버가 기대 서명을 만들 때 사용한다.
	 */
	public static String hmacSha256Hex(String secret, String message) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return toHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 두 16진수 서명 문자열을 상수 시간(constant-time)으로 비교한다.
	 *
	 * <p>일반 문자열 비교(equals)는 앞부분이 틀리면 즉시 종료되어 비교에 걸린 시간으로
	 * 정답을 추측하는 타이밍 공격에 취약합니다. {@link MessageDigest#isEqual} 은
	 * 길이에 무관하게 일정한 시간으로 비교하므로 서명 검증에 적합합니다.</p>
	 */
	public static boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null) {
			return false;
		}
		return MessageDigest.isEqual(
				a.getBytes(StandardCharsets.UTF_8),
				b.getBytes(StandardCharsets.UTF_8));
	}

	/** 바이트 배열을 소문자 16진수 문자열로 변환한다. */
	private static String toHex(byte[] bytes) {
		char[] out = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			out[i * 2] = HEX[v >>> 4];
			out[i * 2 + 1] = HEX[v & 0x0F];
		}
		return new String(out);
	}
}
