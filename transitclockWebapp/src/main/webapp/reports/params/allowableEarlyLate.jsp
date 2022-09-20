<%--
  Created by IntelliJ IDEA.
  User: lenny
  Date: 9/6/22
  Time: 1:05 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="org.transitclock.reports.ReportsConfig" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="row param">
  <div class="col-sm-5 label">Allowable Early:</div>
  <div class="col-sm-7">
    <input id="allowableEarly" name="allowableEarly"
           title="How early a vehicle can arrive compared to the prediction
    	and still be acceptable. Must be a negative number to indicate
    	early."
           size="1"
           type="text"
           placeholder="minutes"
           value=<% out.print(ReportsConfig.getDefaultAllowableEarlyMinutes()); %>
         />
  </div>
</div>

<div class="row param">
  <div class="col-sm-5 label">Allowable Late:</div>
  <div class="col-sm-7">
    <input id="allowableLate" name="allowableLate"
           title="How late a vehicle can arrive compared to the prediction
    	and still be acceptable. Must be a positive number to indicate
    	late."
           size="1"
           type="text"
           placeholder="minutes"
           value=<% out.print(ReportsConfig.getDefaultAllowableLateMinutes()); %>
       />
  </div>
</div>
