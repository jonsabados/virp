<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<html>
	<head>
		<title>Hello Virp User</title>
	</head>
	<body>
		<h3>Welcome Virp User</h3>
		
		<form:form method="post" action="index.html" commandName="form">
			<p>Lets start with your email address: <form:input path="email"/></p>
			<input type="submit" value="Lets start virping!" />
		</form:form>
	</body>
</html>