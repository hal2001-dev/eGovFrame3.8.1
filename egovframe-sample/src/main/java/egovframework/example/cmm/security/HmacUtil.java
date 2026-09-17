package egovframework.example.cmm.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC / SHA-256 helpers for server-to-server request signing.
 * Uses only the JDK (no external dependency).
 */
public final class HmacUtil {

	private static final char[] HEX = "0123456789abcdef".toCharArray();

	private HmacUtil() {
	}

	/** lowercase hex of SHA-256(data) */
	public static String sha256Hex(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return toHex(md.digest(data == null ? new byte[0] : data));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/** lowercase hex of HMAC-SHA256(secret, message) */
	public static String hmacSha256Hex(String secret, String message) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return toHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/** constant-time compare of two hex signatures */
	public static boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null) {
			return false;
		}
		return MessageDigest.isEqual(
				a.getBytes(StandardCharsets.UTF_8),
				b.getBytes(StandardCharsets.UTF_8));
	}

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
