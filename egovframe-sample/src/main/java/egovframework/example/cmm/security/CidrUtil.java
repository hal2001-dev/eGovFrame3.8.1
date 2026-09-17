package egovframework.example.cmm.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * IP allow-list matching by CIDR.
 *
 * <p>Supports IPv4 and IPv6. An entry may be:</p>
 * <ul>
 *   <li>a CIDR block, e.g. {@code 203.0.113.0/24} (class C), {@code 10.1.0.0/16}</li>
 *   <li>a single address, e.g. {@code 198.51.100.10} (treated as /32 or /128)</li>
 * </ul>
 */
public final class CidrUtil {

	private CidrUtil() {
	}

	/** parse a comma/space separated list of CIDR entries */
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

	/** true if ip matches any entry in the list (empty list -> false) */
	public static boolean matchesAny(List<String> cidrs, String ip) {
		for (String cidr : cidrs) {
			if (matches(cidr, ip)) {
				return true;
			}
		}
		return false;
	}

	/** true if the given ip is inside the given CIDR block (or equals a bare IP) */
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
				prefix = -1; // full-length match
			}

			byte[] baseBytes = InetAddress.getByName(base).getAddress();
			byte[] ipBytes = InetAddress.getByName(ip).getAddress();

			// different address families (IPv4 vs IPv6) never match
			if (baseBytes.length != ipBytes.length) {
				return false;
			}
			if (prefix < 0) {
				prefix = baseBytes.length * 8;
			}
			if (prefix < 0 || prefix > baseBytes.length * 8) {
				return false;
			}

			int fullBytes = prefix / 8;
			for (int i = 0; i < fullBytes; i++) {
				if (baseBytes[i] != ipBytes[i]) {
					return false;
				}
			}
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
