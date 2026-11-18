<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="item" required="true" type="com.partyplaylist.dto.QueueItem" %>
<%@ attribute name="party" required="true" type="com.partyplaylist.model.Party" %>
<%@ attribute name="isHost" required="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="finished" value="${party.status == 'FINISHED'}"/>
<c:set var="mine" value="${item.suggestedById == sessionScope.user.id}"/>
<article class="track">
    <div class="track__cover">
        <c:choose>
            <c:when test="${not empty item.coverPath}">
                <img src="${ctx}/media?track=${item.trackId}&amp;type=cover" alt="">
            </c:when>
            <c:otherwise>
                <span class="track__cover-empty">♪</span>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="track__info">
        <div class="track__title"><c:out value="${item.title}"/></div>
        <div class="track__artist"><c:out value="${item.artist}"/></div>
        <div class="track__meta">
            <c:if test="${not empty item.genreName}"><c:out value="${item.genreName}"/> · </c:if>
            энергия ${item.energy} ·
            ${item.sourceType == 'YOUTUBE' ? 'YouTube' : 'mp3'} ·
            предложил <c:out value="${item.suggestedBy}"/>
        </div>
    </div>

    <div class="track__votes">
        <c:if test="${not finished}">
            <form method="post" action="${ctx}/party/vote">
                <t:csrf/>
                <input type="hidden" name="partyId" value="${party.id}">
                <input type="hidden" name="trackId" value="${item.trackId}">
                <input type="hidden" name="value" value="1">
                <button class="vote${item.myVote == 1 ? ' vote--active' : ''}" type="submit" title="Нравится">▲</button>
            </form>
        </c:if>
        <span class="track__rating">${item.rating}</span>
        <c:if test="${not finished}">
            <form method="post" action="${ctx}/party/vote">
                <t:csrf/>
                <input type="hidden" name="partyId" value="${party.id}">
                <input type="hidden" name="trackId" value="${item.trackId}">
                <input type="hidden" name="value" value="-1">
                <button class="vote${item.myVote == -1 ? ' vote--active' : ''}" type="submit" title="Не нравится">▼</button>
            </form>
        </c:if>
    </div>

    <c:if test="${not finished and (mine or isHost)}">
        <div class="track__actions">
            <c:if test="${mine}">
                <a class="btn btn--small btn--ghost" href="${ctx}/party/track/edit?id=${item.trackId}">Изменить</a>
                <form method="post" action="${ctx}/party/track/delete" data-confirm="Удалить этот трек?">
                    <t:csrf/>
                    <input type="hidden" name="partyId" value="${party.id}">
                    <input type="hidden" name="trackId" value="${item.trackId}">
                    <button class="btn btn--small btn--danger" type="submit">Удалить</button>
                </form>
            </c:if>
            <c:if test="${isHost and not mine}">
                <form method="post" action="${ctx}/party/reject" data-confirm="Отклонить этот трек?">
                    <t:csrf/>
                    <input type="hidden" name="partyId" value="${party.id}">
                    <input type="hidden" name="trackId" value="${item.trackId}">
                    <button class="btn btn--small btn--danger" type="submit">Отклонить</button>
                </form>
            </c:if>
        </div>
    </c:if>
</article>
