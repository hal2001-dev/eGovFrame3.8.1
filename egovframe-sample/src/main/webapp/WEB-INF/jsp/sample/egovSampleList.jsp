<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>eGovFrame Sample - List</title>
<style>
	body { font-family: -apple-system, sans-serif; margin: 40px; color: #222; }
	h1 { font-size: 20px; }
	table { border-collapse: collapse; width: 100%; margin-top: 12px; }
	th, td { border: 1px solid #ccc; padding: 8px 10px; text-align: left; font-size: 14px; }
	th { background: #f4f6f8; }
	.toolbar { margin: 12px 0; }
	a.btn, button { padding: 6px 12px; border: 1px solid #3367d6; background: #4285f4; color: #fff;
		border-radius: 4px; text-decoration: none; font-size: 13px; cursor: pointer; }
	.count { color: #666; font-size: 13px; }
</style>
</head>
<body>
	<h1>eGovFrame 3.8 Sample &mdash; 게시판 목록</h1>
	<p class="count">총 <strong>${resultCnt}</strong> 건</p>

	<form action="<c:url value='/action/sample/egovSampleList.do'/>" method="get" class="toolbar">
		<select name="searchCondition">
			<option value="">전체</option>
			<option value="0" ${searchVO.searchCondition eq '0' ? 'selected' : ''}>이름</option>
			<option value="1" ${searchVO.searchCondition eq '1' ? 'selected' : ''}>설명</option>
		</select>
		<input type="text" name="searchKeyword" value="${searchVO.searchKeyword}" placeholder="검색어" />
		<button type="submit">검색</button>
		<a class="btn" href="<c:url value='/action/sample/addSample.do'/>">신규 등록</a>
	</form>

	<table>
		<thead>
			<tr>
				<th style="width:160px">ID</th>
				<th>이름</th>
				<th>설명</th>
				<th style="width:60px">사용</th>
				<th style="width:140px">관리</th>
			</tr>
		</thead>
		<tbody>
			<c:choose>
				<c:when test="${empty resultList}">
					<tr><td colspan="5" style="text-align:center; color:#888">데이터가 없습니다.</td></tr>
				</c:when>
				<c:otherwise>
					<c:forEach var="row" items="${resultList}">
						<tr>
							<td>${row.id}</td>
							<td><c:out value="${row.name}" /></td>
							<td><c:out value="${row.description}" /></td>
							<td>${row.useYn}</td>
							<td>
								<a href="<c:url value='/action/sample/updateSampleView.do?selectedId=${row.id}'/>">수정</a>
								|
								<a href="<c:url value='/action/sample/deleteSample.do?selectedId=${row.id}'/>"
									onclick="return confirm('삭제하시겠습니까?');">삭제</a>
							</td>
						</tr>
					</c:forEach>
				</c:otherwise>
			</c:choose>
		</tbody>
	</table>
</body>
</html>
