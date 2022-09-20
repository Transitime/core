<%--
  Created by IntelliJ IDEA.
  User: lenny
  Date: 9/6/22
  Time: 3:15 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<div class="param row">
  <div class="col-sm-5 label">Intervals Type:</div>
  <div class="col-sm-7">
    <select id="intervalsType" name="intervalsType"
            title="Specifies the type of graph to be displayed. By selecting
     		'Percentage only' the charge will display the prediction accuracy
     		range that is within the percentages specified below. By selecting
     		'Standard Deviation only' the chart will display the prediction
     		accuracy range for predictions within a standard deviation of the
     		mean. This method is experimental and might be removed. By
     		selecting 'Percentage only and Standard Deviation' the chart will
     		display both a percentage interval and a standard deviation
     		interval.">
      <option value="PERCENTAGE">Percentage only</option>
      <option value="STD_DEV">Standard Deviation only</option>
      <option value="BOTH">Percentage only and Standard Deviation</option>
    </select>
  </div>
</div>

<div class="param row">
  <div class="col-sm-5 label">Interval Percentage 1:</div>
  <div class="col-sm-7">
    <input id="intervalPercentage1" name="intervalPercentage1"
           title="For when using a 'Percentage' interval type. Specifies the
                 percent of predictions that should lie within the minimum
                 and maximum intervals."
           type="number"
           value="70"
           min="0"
           placeholder="%"
           max="100" required />
  </div>
</div>

<div class="param row">

  <div class="col-sm-5 label">Interval Percentage 2:</div>
  <div class="col-sm-7">
    <input id="intervalPercentage2" name="intervalPercentage2"
           title="Optional value for when using a 'Percentage' interval type.
    		Specifies a second percent of predictions that should lie within
    		the minimum and maximum intervals."
           type="number"
           min="0"
           placeholder="Optional %"
           max="100"/>
  </div>
</div>
