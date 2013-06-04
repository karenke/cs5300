<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Session Management</title>
	<script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
	<script type="text/javascript">
		function updateSession() {
			$.ajax({
		        url: '/SessionProject/SessionServlet',
		        type: 'GET',
		        data: "cmd=Refresh",
		        success: function(data) {
		        	data = data.split("\n");
					//alert(data);
 		        	$('#sessLocation').html(data[0]);
		        	$('#expTime').html(data[1]);
		        	$('#servletMessage').html(data[2]); 
		        },
		        error: function(data) {
		        	alert("session update failure!");

		        }
		    });
		}
		function replaceMessage() {
			$.ajax({
		        url: '/SessionProject/SessionServlet',
		        type: 'GET',
		        data: "cmd=Replace&newMsg=" + $('#submitMessage').val(),
		        success: function(data) {
		        	data = data.split("\n");
					//alert(data);
		        	$('#sessLocation').html(data[0]);
		        	$('#expTime').html(data[1]);
		        	$('#servletMessage').html(data[2]);
		        },
		        error: function(data) {
		        	alert("session message replace failure!");
		        }
		    });
		}
		function logout() {
			$.ajax({
		        url: '/SessionProject/SessionServlet',
		        type: 'GET',
		        data: "cmd=Logout",
		        success: function(data) {
		        	$('body').html(data);
		        },
		        error: function(data) {

		        }
		    });
		}
		
		$(window).load(function () {
			updateSession();
		});
		$(document).ready(function(){
			$('#f1').submit(function(e) {
				  e.preventDefault();
				  updateSession();
			}),
			
			$('#f2').submit(function(e) {
				  e.preventDefault();
				  replaceMessage();
			}),
			
			$('#f3').submit(function(e) {
				  e.preventDefault();
				  logout();
			});
		});
		
		
		self.addEventListener('message', function(evt) {
	        var gateway = new XMLHttpRequest();
	        var intie = setInterval(function() {
	        gateway.open("GET",evt.data.load_url,true);
	        gateway.send();
	        gateway.onreadystatechange = function() {
	            if (gateway.readyState==4 && gateway.status==200)
	                self.postMessage(gateway.responseText);
	        }
	   }, 300000); //every 5 minutes
	}, false);
		
	</script>

</head>



<body>
<h1><font face="Times New Roman" color="#000000" size="6">CS5300 Spring 2013 Project1a</font></h1>
	<h1 id="servletMessage"></h1>
	<table>
		<tr>
			<td>
				<form id ="f1" name ="f1">
					<input type="submit" value="Refresh"> 
				</form>
			</td>
		</tr>
	</table>
	
	<form id ="f2" name ="f2" >
	<table>	
		<tr>
			<td><input type=submit name=cmd value=Replace></td>
			<td><input id="submitMessage" type="text" name = "newMsg"></input></td>	
		</tr>
	</table>
	</form>
	<table>
		<tr>
			<td>
				<form id ="f3" name ="f3" >
					<input type=submit name=cmd value= "Log out"> 
				</form>
			</td>
		</tr>
		<tr>
			<td>Session On: </td>
			<td id = "sessLocation"></td>
		</tr>
		<tr>
			<td>Expiration Time: </td>
			<td id = "expTime"></td>
		</tr>
	</table>
</body>
</html>