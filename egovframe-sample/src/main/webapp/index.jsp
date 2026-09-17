<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 루트 접근 시 게시판 목록(화면 액션)으로 이동 --%>
<% response.sendRedirect(request.getContextPath() + "/action/sample/egovSampleList.do"); %>
