<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Ошибка сервера">
    <section class="error-page">
        <h1>500</h1>
        <p>Что-то пошло не так. Попробуй ещё раз чуть позже.</p>
        <a class="btn" href="${pageContext.request.contextPath}/">На главную</a>
    </section>
</t:layout>
