<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="editing" value="${not empty track}"/>
<t:layout title="${editing ? 'Изменить трек' : 'Предложить трек'}">
    <section class="form-page">
        <h1>${editing ? 'Изменить трек' : 'Предложить трек'}</h1>
        <p class="muted">Вечеринка: <c:out value="${party.title}"/></p>

        <form class="form" method="post" enctype="multipart/form-data"
              action="${ctx}${editing ? '/party/track/edit' : '/party/suggest'}">
            <t:csrf/>
            <c:choose>
                <c:when test="${editing}">
                    <input type="hidden" name="id" value="${track.id}">
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="partyId" value="${party.id}">
                </c:otherwise>
            </c:choose>

            <t:field name="title" label="Название" value="${form.title}" error="${errors.title}"/>
            <t:field name="artist" label="Исполнитель" value="${form.artist}" error="${errors.artist}"/>

            <div class="field${not empty errors.genreId ? ' field--error' : ''}">
                <label class="field__label" for="genreId">Жанр</label>
                <select class="field__input" id="genreId" name="genreId">
                    <option value="">Не указан</option>
                    <c:forEach var="genre" items="${genres}">
                        <option value="${genre.id}" ${form.genreId == ('' += genre.id) ? 'selected' : ''}>
                            <c:out value="${genre.name}"/>
                        </option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.genreId}">
                    <span class="field__error"><c:out value="${errors.genreId}"/></span>
                </c:if>
            </div>

            <div class="field${not empty errors.energy ? ' field--error' : ''}">
                <label class="field__label" for="energy">Энергия трека</label>
                <select class="field__input" id="energy" name="energy">
                    <option value="1" ${form.energy == '1' ? 'selected' : ''}>1 — совсем спокойный</option>
                    <option value="2" ${form.energy == '2' ? 'selected' : ''}>2 — спокойный</option>
                    <option value="3" ${form.energy == '3' ? 'selected' : ''}>3 — средний</option>
                    <option value="4" ${form.energy == '4' ? 'selected' : ''}>4 — энергичный</option>
                    <option value="5" ${form.energy == '5' ? 'selected' : ''}>5 — взрывной</option>
                </select>
                <c:if test="${not empty errors.energy}">
                    <span class="field__error"><c:out value="${errors.energy}"/></span>
                </c:if>
            </div>

            <div class="field${not empty errors.sourceType ? ' field--error' : ''}">
                <span class="field__label">Как добавить музыку</span>
                <div class="radio-group">
                    <label class="radio">
                        <input type="radio" name="sourceType" value="FILE" ${form.sourceType == 'FILE' ? 'checked' : ''}>
                        mp3-файл
                    </label>
                    <label class="radio">
                        <input type="radio" name="sourceType" value="YOUTUBE" ${form.sourceType == 'YOUTUBE' ? 'checked' : ''}>
                        Ссылка на YouTube
                    </label>
                </div>
                <c:if test="${not empty errors.sourceType}">
                    <span class="field__error"><c:out value="${errors.sourceType}"/></span>
                </c:if>
            </div>

            <div class="field${not empty errors.audio ? ' field--error' : ''}" data-source="FILE">
                <label class="field__label" for="audio">mp3-файл</label>
                <input class="field__input" id="audio" name="audio" type="file" accept=".mp3,audio/mpeg">
                <c:if test="${editing and track.file}">
                    <span class="field__hint">Файл уже загружен. Выберите новый, только если хотите заменить его.</span>
                </c:if>
                <c:if test="${not empty errors.audio}">
                    <span class="field__error"><c:out value="${errors.audio}"/></span>
                </c:if>
            </div>

            <div data-source="YOUTUBE">
                <t:field name="youtubeUrl" label="Ссылка на YouTube" value="${form.youtubeUrl}"
                         error="${errors.youtubeUrl}" placeholder="https://www.youtube.com/watch?v=..."/>
            </div>

            <div class="field${not empty errors.cover ? ' field--error' : ''}">
                <label class="field__label" for="cover">Обложка (необязательно)</label>
                <input class="field__input" id="cover" name="cover" type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png">
                <c:if test="${editing and not empty track.coverPath}">
                    <span class="field__hint">Обложка уже есть. Выберите новую, только если хотите заменить её.</span>
                </c:if>
                <c:if test="${not empty errors.cover}">
                    <span class="field__error"><c:out value="${errors.cover}"/></span>
                </c:if>
            </div>

            <c:if test="${not empty errors}">
                <p class="field__hint">Если вы выбирали файлы, выберите их ещё раз — браузер не сохраняет их после ошибки.</p>
            </c:if>

            <div class="form__buttons">
                <button class="btn" type="submit">${editing ? 'Сохранить' : 'Предложить'}</button>
                <a class="btn btn--ghost" href="${ctx}/party?id=${party.id}">Отмена</a>
            </div>
        </form>
    </section>
    <script src="${ctx}/static/js/track-form.js"></script>
</t:layout>
