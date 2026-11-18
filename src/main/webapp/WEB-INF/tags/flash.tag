<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty sessionScope.flash}">
    <div class="flash flash--ok"><c:out value="${sessionScope.flash}"/></div>
    <c:remove var="flash" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.flashError}">
    <div class="flash flash--error"><c:out value="${sessionScope.flashError}"/></div>
    <c:remove var="flashError" scope="session"/>
</c:if>
