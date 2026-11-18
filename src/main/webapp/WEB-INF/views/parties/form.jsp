<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="editing" value="${not empty party}"/>
<t:layout title="${editing ? 'Настройки вечеринки' : 'Новая вечеринка'}">
    <section class="form-page">
        <h1>${editing ? 'Настройки вечеринки' : 'Новая вечеринка'}</h1>

        <form class="form" method="post" action="${ctx}${editing ? '/parties/edit' : '/parties/new'}">
            <t:csrf/>
            <c:if test="${editing}">
                <input type="hidden" name="id" value="${party.id}">
            </c:if>
            <t:field name="title" label="Название" value="${title}" error="${errors.title}"
                     placeholder="например, День рождения Димы"/>
            <t:field name="maxSuggestions" label="Сколько треков может предложить гость" type="number"
                     value="${maxSuggestions}" error="${errors.maxSuggestions}"/>
            <div class="form__buttons">
                <button class="btn" type="submit">${editing ? 'Сохранить' : 'Создать'}</button>
                <c:choose>
                    <c:when test="${editing}">
                        <a class="btn btn--ghost" href="${ctx}/party?id=${party.id}">Отмена</a>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn--ghost" href="${ctx}/parties">Отмена</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </form>

        <c:if test="${editing}">
            <form class="danger-zone" method="post" action="${ctx}/parties/delete"
                  data-confirm="Удалить вечеринку вместе со всеми треками? Это нельзя отменить.">
                <t:csrf/>
                <input type="hidden" name="id" value="${party.id}">
                <button class="btn btn--danger" type="submit">Удалить вечеринку</button>
            </form>
        </c:if>
    </section>
</t:layout>
