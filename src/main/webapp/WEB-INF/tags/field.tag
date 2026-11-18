<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="type" %>
<%@ attribute name="value" %>
<%@ attribute name="error" %>
<%@ attribute name="placeholder" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="field${not empty error ? ' field--error' : ''}">
    <label class="field__label" for="${name}">${label}</label>
    <input class="field__input" id="${name}" name="${name}" type="${empty type ? 'text' : type}"
           value="${fn:escapeXml(value)}" placeholder="${fn:escapeXml(placeholder)}">
    <c:if test="${not empty error}">
        <span class="field__error"><c:out value="${error}"/></span>
    </c:if>
</div>
