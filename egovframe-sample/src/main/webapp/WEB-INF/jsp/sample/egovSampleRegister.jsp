<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isEdit" value="${not empty sampleVO.id}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>eGovFrame Sample - Register</title>
<style>
	body { font-family: -apple-system, sans-serif; margin: 40px; color: #222; }
	h1 { font-size: 20px; }
	label { display: block; margin: 12px 0 4px; font-size: 13px; color: #444; }
	input[type=text], textarea, select { width: 420px; padding: 6px; font-size: 14px; }
	textarea { height: 90px; }
	.actions { margin-top: 18px; }
	button, a.btn { padding: 6px 14px; border: 1px solid #3367d6; background: #4285f4; color: #fff;
		border-radius: 4px; text-decoration: none; font-size: 13px; cursor: pointer; }
	a.cancel { background: #888; border-color: #666; }
</style>
</head>
<body>
	<h1>eGovFrame 3.8 Sample &mdash; ${isEdit ? '수정' : '신규 등록'}</h1>

	<form action="<c:url value='${isEdit ? "/action/sample/updateSample.do" : "/action/sample/addSample.do"}'/>" method="post">
		<c:if test="${isEdit}">
			<input type="hidden" name="id" value="${sampleVO.id}" />
		</c:if>
		<label>이름</label>
		<input type="text" name="name" value="<c:out value='${sampleVO.name}'/>" required />

		<label>설명</label>
		<textarea name="description"><c:out value='${sampleVO.description}'/></textarea>

		<label>사용여부</label>
		<select name="useYn">
			<option value="Y" ${sampleVO.useYn ne 'N' ? 'selected' : ''}>Y</option>
			<option value="N" ${sampleVO.useYn eq 'N' ? 'selected' : ''}>N</option>
		</select>

		<label>등록자</label>
		<input type="text" name="regUser" value="<c:out value='${sampleVO.regUser}'/>" />

		<div class="actions">
			<button type="submit">${isEdit ? '수정' : '등록'}</button>
			<a class="btn cancel" href="<c:url value='/action/sample/egovSampleList.do'/>">취소</a>
		</div>
	</form>
</body>
</html>
