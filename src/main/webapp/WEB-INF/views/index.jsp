<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Главная">
    <section class="hero">
        <h1>Плейлист, который собирают гости</h1>
        <p class="hero__lead">
            Создай вечеринку, поделись кодом — гости предлагают треки и голосуют,
            а очередь подстраивается под фазу вечера.
        </p>
    </section>

    <section class="phases">
        <c:forEach var="phase" items="${phases}">
            <article class="phase-card phase-card--${phase.key}">
                <h2><c:out value="${phase.title}"/></h2>
                <p>Энергия треков: ${phase.minEnergy}–${phase.maxEnergy}</p>
            </article>
        </c:forEach>
    </section>
</t:layout>
