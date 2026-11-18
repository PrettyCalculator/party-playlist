<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="finished" value="${party.status == 'FINISHED'}"/>
<c:set var="live" value="${party.status == 'LIVE'}"/>
<t:layout title="${party.title}" refresh="${isHost ? '' : '10'}">
    <section class="party-head">
        <div>
            <h1><c:out value="${party.title}"/></h1>
            <div class="party-head__badges">
                <span class="badge badge--${party.status.key}">${party.status.title}</span>
                <span class="badge badge--${party.phase.key}">${party.phase.title}</span>
            </div>
        </div>
        <div class="party-head__code">
            <span class="muted">Код для гостей</span>
            <strong>${party.joinCode}</strong>
        </div>
    </section>

    <c:if test="${isHost}">
        <section class="panel host-panel">
            <h2 class="panel__title">Пульт хоста</h2>

            <c:if test="${not empty current}">
                <div class="player">
                    <c:choose>
                        <c:when test="${current.sourceType == 'FILE'}">
                            <audio id="player" src="${ctx}/media?track=${current.trackId}" controls autoplay></audio>
                        </c:when>
                        <c:otherwise>
                            <div id="youtube-player" data-video-id="${youtubeId}"></div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <div class="host-panel__buttons">
                <c:if test="${party.status == 'DRAFT'}">
                    <form method="post" action="${ctx}/party/start">
                        <t:csrf/>
                        <input type="hidden" name="partyId" value="${party.id}">
                        <button class="btn" type="submit">Начать вечеринку</button>
                    </form>
                </c:if>

                <c:if test="${live}">
                    <form id="next-form" method="post" action="${ctx}/party/next">
                        <t:csrf/>
                        <input type="hidden" name="partyId" value="${party.id}">
                        <button class="btn" type="submit">${empty current ? 'Включить музыку' : 'Следующий трек'}</button>
                    </form>

                    <form class="phase-form" method="post" action="${ctx}/party/phase">
                        <t:csrf/>
                        <input type="hidden" name="partyId" value="${party.id}">
                        <select class="field__input" name="phase">
                            <c:forEach var="phase" items="${phases}">
                                <option value="${phase}" ${party.phase == phase ? 'selected' : ''}>${phase.title}</option>
                            </c:forEach>
                        </select>
                        <button class="btn btn--ghost" type="submit">Сменить фазу</button>
                    </form>

                    <form method="post" action="${ctx}/party/finish" data-confirm="Завершить вечеринку? Голосовать и предлагать треки будет нельзя.">
                        <t:csrf/>
                        <input type="hidden" name="partyId" value="${party.id}">
                        <button class="btn btn--danger" type="submit">Завершить</button>
                    </form>
                </c:if>

                <a class="btn btn--ghost" href="${ctx}/party?id=${party.id}">Обновить очередь</a>
                <a class="btn btn--ghost" href="${ctx}/parties/edit?id=${party.id}">Настройки</a>
            </div>

            <c:if test="${party.status == 'DRAFT'}">
                <p class="muted">Пока вечеринка не началась, гости уже могут заходить по коду и предлагать треки.</p>
            </c:if>
        </section>
    </c:if>

    <section class="panel now-playing">
        <h2 class="panel__title">Сейчас играет</h2>
        <c:choose>
            <c:when test="${not empty current}">
                <div class="now-playing__track">
                    <strong><c:out value="${current.title}"/></strong>
                    <span class="muted"><c:out value="${current.artist}"/></span>
                </div>
            </c:when>
            <c:otherwise>
                <p class="muted">Ничего не играет.</p>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="queue">
        <div class="page-head">
            <h2>Очередь</h2>
            <c:if test="${not finished}">
                <a class="btn" href="${ctx}/party/suggest?partyId=${party.id}">Предложить трек</a>
            </c:if>
        </div>
        <c:choose>
            <c:when test="${empty queue}">
                <p class="empty">В очереди пока нет треков.</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="item" items="${queue}">
                    <t:trackCard item="${item}" party="${party}" isHost="${isHost}"/>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </section>

    <c:if test="${not isHost}">
        <form class="danger-zone" method="post" action="${ctx}/parties/leave" data-confirm="Выйти из вечеринки?">
            <t:csrf/>
            <input type="hidden" name="id" value="${party.id}">
            <button class="btn btn--ghost" type="submit">Выйти из вечеринки</button>
        </form>
    </c:if>

    <c:if test="${isHost}">
        <script src="${ctx}/static/js/player.js"></script>
    </c:if>
</t:layout>
