package egovframework.example.cmm.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * CIDR 기반 IP 화이트리스트 매칭 유틸리티.
 *
 * <p>IPv4/IPv6 를 모두 지원하며, 허용 목록의 각 항목은 다음 형식일 수 있습니다.</p>
 * <ul>
 *   <li>CIDR 대역: {@code 203.0.113.0/24}(C 클래스), {@code 10.1.0.0/16}(B 클래스)</li>
 *   <li>단일 IP: {@code 198.51.100.10} (내부적으로 /32(IPv4) 또는 /128(IPv6) 로 처리)</li>
 * </ul>
 *
 * <p>주소를 바이트 배열로 변환한 뒤 프리픽스 길이만큼 비트 단위로 비교하는 방식이라,
 * 문자열 파싱 없이 정확하게 대역 포함 여부를 판정합니다.</p>
 */
public final class CidrUtil {

	/** 인스턴스화 방지(정적 유틸리티 클래스) */
	private CidrUtil() {
	}

	/**
	 * 콤마 또는 공백으로 구분된 CIDR 목록 문자열을 리스트로 파싱한다.
	 * (예: {@code "203.0.113.0/24, 10.0.0.5"} → ["203.0.113.0/24", "10.0.0.5"])
	 */
	public static List<String> parseList(String csv) {
		List<String> out = new ArrayList<String>();
		if (csv == null) {
			return out;
		}
		for (String part : csv.split("[,\\s]+")) {
			String p = part.trim();
			if (!p.isEmpty()) {
				out.add(p);
			}
		}
		return out;
	}

	/** 주어진 IP 가 목록 중 하나라도 매칭되면 true. (빈 목록이면 false) */
	public static boolean matchesAny(List<String> cidrs, String ip) {
		for (String cidr : cidrs) {
			if (matches(cidr, ip)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 주어진 IP 가 특정 CIDR 대역에 포함되는지(또는 단일 IP 와 일치하는지) 판정한다.
	 *
	 * <p>동작: 기준 주소와 대상 IP 를 각각 바이트 배열로 변환한 뒤, 프리픽스 비트 수만큼
	 * (완전한 바이트 + 남은 비트)를 비교한다. IPv4 와 IPv6 처럼 주소 체계가 다르면
	 * 항상 false. 파싱 오류/알 수 없는 호스트도 false 로 안전하게 처리한다.</p>
	 */
	public static boolean matches(String cidr, String ip) {
		try {
			String base;
			int prefix;
			int slash = cidr.indexOf('/');
			if (slash >= 0) {
				base = cidr.substring(0, slash).trim();
				prefix = Integer.parseInt(cidr.substring(slash + 1).trim());
			} else {
				base = cidr.trim();
				prefix = -1; // 프리픽스가 없으면 주소 전체 길이로 비교(단일 IP)
			}

			byte[] baseBytes = InetAddress.getByName(base).getAddress();
			byte[] ipBytes = InetAddress.getByName(ip).getAddress();

			// 주소 체계(IPv4 4바이트 vs IPv6 16바이트)가 다르면 매칭되지 않음
			if (baseBytes.length != ipBytes.length) {
				return false;
			}
			if (prefix < 0) {
				prefix = baseBytes.length * 8;
			}
			if (prefix < 0 || prefix > baseBytes.length * 8) {
				return false;
			}

			// 1) 완전히 채워지는 바이트들은 그대로 일치해야 한다
			int fullBytes = prefix / 8;
			for (int i = 0; i < fullBytes; i++) {
				if (baseBytes[i] != ipBytes[i]) {
					return false;
				}
			}
			// 2) 남은 비트(prefix % 8)는 상위 비트 마스크로 비교한다
			int remainingBits = prefix % 8;
			if (remainingBits > 0) {
				int mask = 0xFF << (8 - remainingBits) & 0xFF;
				if ((baseBytes[fullBytes] & mask) != (ipBytes[fullBytes] & mask)) {
					return false;
				}
			}
			return true;
		} catch (UnknownHostException | NumberFormatException e) {
			return false;
		}
	}
}
