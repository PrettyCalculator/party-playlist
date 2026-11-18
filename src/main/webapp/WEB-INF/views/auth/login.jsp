<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Вход">
    <section class="auth">
        <h1>Вход</h1>
        <c:if test="${not empty errors.login}">
            <div class="flash flash--error"><c:out value="${errors.login}"/></div>
        </c:if>
        <form class="form" method="post" action="${pageContext.request.contextPath}/login">
            <t:csrf/>
            <t:field name="username" label="Логин" value="${username}"/>
            <t:field name="password" label="Пароль" type="password"/>
            <button class="btn" type="submit">Войти</button>
        </form>
        <p class="auth__switch">Нет аккаунта? <a href="${pageContext.request.contextPath}/register">Зарегистрироваться</a></p>
    </section>
</t:layout>
