<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
	<head>
		<title>Virps</title>
	</head>
	<body>
		<h3>Virps</h3>
		
		<p>
			Found a total of <fmt:formatNumber value="${virpCount}"/> virps with 
			<fmt:formatNumber value="${fn:length(activeVirps)}" /> detailed records.
			<c:if test="${averageRating != null}"> 
				Average star rating: <fmt:formatNumber value="${averageRating}" 
										minFractionDigits="1" maxFractionDigits="1"/>
			</c:if>
		</p>
		
		<c:choose>
			<c:when test="${!empty activeVirps}">
				<span>Records with details:</span>
				<table>
					<tr>
						<th>ID</th>
						<th>Notes</th>
						<th>Stars</th>
					</tr>
					<c:forEach items="${activeVirps}" var="virp">
						<tr>
							<td><c:out value="${virp.uuid}" /></td>
							<td><pre><c:out value="${virp.notes}" /></pre></td>
							<td><fmt:formatNumber value="${virp.starRating}" /></td>
						</tr>
					</c:forEach>
				</table>
			</c:when>
		</c:choose>
		
		<h2>Create a new virp:</h2>
		<div>
			<form method="post" action="my_virps.html">
				<div class="input">
					<span class="label">Notes:</span>
					<textarea name="notes" cols="30" rows="5"></textarea>
				</div>
				<div class="input">
					<span class="label">Star rating:</span>
					<input type="text" name="starRating"/>
				</div>
				<div class="input">
					<span class="label">Star retention period:</span>
					<input type="text" name="starRatingRetention" value="120"/>
				</div>
				<input type="submit" value="Create Virp" />
			</form>
		</div>
	</body>
</html>