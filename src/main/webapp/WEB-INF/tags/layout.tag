<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="refresh" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <c:if test="${not empty refresh}">
        <meta http-equiv="refresh" content="${refresh}">
    </c:if>
    <title><c:out value="${title}"/> · Party Playlist</title>
    <link rel="stylesheet" href="${ctx}/static/css/style.css">
</head>
<body>
<header class="header">
    <div class="container header__inner">
        <a class="logo" href="${ctx}/">Party<span>Playlist</span></a>
        <nav class="nav">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <a href="${ctx}/parties">Мои вечеринки</a>
                    <span class="nav__user"><c:out value="${sessionScope.user.username}"/></span>
                    <form class="nav__logout" method="post" action="${ctx}/logout">
                        <t:csrf/>
                        <button class="link-button" type="submit">Выйти</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/login">Войти</a>
                    <a href="${ctx}/register">Регистрация</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>

<main class="container main">
    <t:flash/>
    <jsp:doBody/>
</main>

<footer class="footer">
    <div class="container">Party Playlist · семестровая работа</div>
</footer>
<script src="${ctx}/static/js/app.js"></script>
</body>
</html>
