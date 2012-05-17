<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<html>
	<head>
		<title>User Details</title>
	</head>
	<body>
		<h3>User Details</h3>
		
		<form:form method="post" action="edit_user.html" commandName="user">
			<div class="input">
				<span class="label">Email address:</span>
				<form:input path="email"/>
			</div>
			<div class="input">
				<span class="label">First name:</span>
				<form:input path="firstName"/>
			</div>
			<div class="input">
				<span class="label">Last name:</span>
				<form:input path="lastName"/>
			</div>
			<input type="submit" value="Lets start virping!" />
		</form:form>
	</body>
</html>