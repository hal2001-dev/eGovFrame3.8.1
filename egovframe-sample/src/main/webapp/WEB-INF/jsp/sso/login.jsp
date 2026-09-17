<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>SSO 로그인 (데모)</title>
<style>
	body { font-family: -apple-system, sans-serif; margin: 60px auto; max-width: 380px; color: #222; }
	h1 { font-size: 18px; }
	.card { border: 1px solid #ccc; border-radius: 8px; padding: 22px; }
	label { display: block; margin: 12px 0 4px; font-size: 13px; color: #444; }
	input[type=text] { width: 100%; padding: 8px; font-size: 14px; box-sizing: border-box; }
	button { margin-top: 16px; width: 100%; padding: 9px; border: 1px solid #3367d6;
		background: #4285f4; color: #fff; border-radius: 4px; font-size: 14px; cursor: pointer; }
	.hint { color: #888; font-size: 12px; margin-top: 14px; }
</style>
</head>
<body>
	<h1>SSO 로그인 (데모)</h1>
	<div class="card">
		<p style="font-size:13px;color:#555">실제 환경에서는 이 화면 대신 SSO 서버(IdP)로 이동합니다.</p>
		<form action="<c:url value='/sso/login'/>" method="post">
			<input type="hidden" name="returnUrl" value="<c:out value='${returnUrl}'/>" />
			<label>사용자 ID</label>
			<input type="text" name="username" value="alice" required />
			<button type="submit">로그인</button>
		</form>
		<p class="hint">
			이동할 곳: <code>${returnUrl}</code><br />
			토큰 방식 예: <code>/secure/home.do?ssoToken=demo-token-alice</code>
		</p>
	</div>
</body>
</html>
