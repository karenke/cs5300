<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Session Management</title>
	

</head>



<body>
<h1><font face="Times New Roman" color="#000000" size="6">CS5300 Spring 2013 Project1a</font></h1>
	<h1 id="servletMessage">${msg}</h1>
	<table>
		<tr>
			<td>
				<form id ="f1" name ="f1" action="SessionServlet" method = "Get">
					<input type="submit" name = cmd value="Refresh"> 
				</form>
			</td>
		</tr>
	</table>
	
	<form id ="f2" name ="f2" action="SessionServlet" method = "Get">
	<table>	
		<tr>	
			<td><input type=submit name= cmd value=Replace></td>
			<td><input id="submitMessage" type="text" name = "newMsg"></input></td>	
		</tr>
	</table>
	</form>
	<table>
		<tr>
			<td>
				<form id ="f3" name ="f3" action="SessionServlet" method = "Get">
					<input type=submit name=cmd value= "Logout"> 
				</form>
			</td>
		</tr>
		<tr>
			<td>Session On: </td>
			<td id = "sessLocation">${location}</td>
		</tr>
		<tr>
			<td>Expiration Time: </td>
			<td id = "expTime">${exp}</td>
		</tr>
	</table>
</body>
</html>