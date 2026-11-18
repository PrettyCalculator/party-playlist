<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="Мои вечеринки">
    <div class="page-head">
        <h1>Мои вечеринки</h1>
        <a class="btn" href="${ctx}/parties/new">Создать вечеринку</a>
    </div>

    <section class="panel">
        <h2 class="panel__title">Войти по коду</h2>
        <form class="form form--inline" method="post" action="${ctx}/parties/join">
            <t:csrf/>
            <t:field name="code" label="Код вечеринки" value="${code}" error="${errors.code}" placeholder="например, K7QX2M"/>
            <button class="btn" type="submit">Войти</button>
        </form>
    </section>

    <c:choose>
        <c:when test="${empty parties}">
            <p class="empty">Вы пока не участвуете ни в одной вечеринке. Создайте свою или войдите по коду.</p>
        </c:when>
        <c:otherwise>
            <section class="cards">
                <c:forEach var="party" items="${parties}">
                    <a class="party-card" href="${ctx}/party?id=${party.id}">
                        <div class="party-card__title"><c:out value="${party.title}"/></div>
                        <div class="party-card__badges">
                            <span class="badge badge--${party.status.key}">${party.status.title}</span>
                            <span class="badge badge--${party.phase.key}">${party.phase.title}</span>
                            <c:if test="${party.hostId == sessionScope.user.id}">
                                <span class="badge">я хост</span>
                            </c:if>
                        </div>
                        <div class="party-card__code">Код: ${party.joinCode}</div>
                    </a>
                </c:forEach>
            </section>
        </c:otherwise>
    </c:choose>
</t:layout>
