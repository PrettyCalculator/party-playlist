<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Нет доступа">
    <section class="error-page">
        <h1>403</h1>
        <p>У вас нет доступа к этой странице.</p>
        <a class="btn" href="${pageContext.request.contextPath}/">На главную</a>
    </section>
</t:layout>
