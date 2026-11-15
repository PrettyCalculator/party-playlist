<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Страница не найдена">
    <section class="error-page">
        <h1>404</h1>
        <p>Такой страницы нет. Возможно, вечеринка уже закончилась.</p>
        <a class="btn" href="${pageContext.request.contextPath}/">На главную</a>
    </section>
</t:layout>
