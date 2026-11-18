<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Регистрация">
    <section class="auth">
        <h1>Регистрация</h1>
        <form class="form" method="post" action="${pageContext.request.contextPath}/register">
            <t:csrf/>
            <t:field name="username" label="Логин" value="${username}" error="${errors.username}"
                     placeholder="латиница, цифры и _"/>
            <t:field name="password" label="Пароль" type="password" error="${errors.password}"/>
            <button class="btn" type="submit">Зарегистрироваться</button>
        </form>
        <p class="auth__switch">Уже есть аккаунт? <a href="${pageContext.request.contextPath}/login">Войти</a></p>
    </section>
</t:layout>
