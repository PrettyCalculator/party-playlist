<%@ tag description="Общий шаблон страницы: head, шапка, контент, подвал" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${title}"/> · Party Playlist</title>
    <link rel="stylesheet" href="${ctx}/static/css/style.css">
</head>
<body>
<header class="header">
    <div class="container header__inner">
        <a class="logo" href="${ctx}/">Party<span>Playlist</span></a>
        <nav class="nav">
            <a href="${ctx}/">Главная</a>
            <%-- ссылки на вечеринки, треки и вход появятся на следующих шагах --%>
        </nav>
    </div>
</header>

<main class="container main">
    <jsp:doBody/>
</main>

<footer class="footer">
    <div class="container">Party Playlist · семестровая работа</div>
</footer>
<script src="${ctx}/static/js/app.js"></script>
</body>
</html>
