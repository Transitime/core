<%@ page import="java.io.*" %>
<%
// Open up the file. Need different names depending on whether
// on local system or on AWS
PrintWriter fileOut;
try {
    fileOut = new PrintWriter(new BufferedWriter(new FileWriter("/usr/share/tomcat7/webapps/api/tracker/data.txt", true)));
} catch (Exception e) {
    try {
	    fileOut = new PrintWriter(new BufferedWriter(new FileWriter("C:/Users/Mike/data.txt", true)));
    } catch (Exception e2) {
        System.out.println(e2);
    	return;
    }
}

// Read the comma separated lines of data from the request
ServletInputStream s = request.getInputStream();
int len = 1000;
byte b[] = new byte[len];
int charsRead;

do {
    charsRead = s.readLine(b, 0, len);
    if (charsRead > 0) {
	    String str = request.getRemoteAddr() + "," + new String(b, 0, charsRead);
	    System.out.print(str);
	    fileOut.print(str);
    }
} while (charsRead > 0);
fileOut.flush();
%>