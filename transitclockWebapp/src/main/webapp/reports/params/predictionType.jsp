<%--
  Created by IntelliJ IDEA.
  User: lenny
  Date: 9/6/22
  Time: 3:12 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="param row">
  <div class="col-sm-5 label">Prediction Type:</div>
  <div class="col-sm-7">
    <select id="predictionType" name="predictionType"
            title="Specifies whether or not to show prediction accuracy for
     	predictions that were affected by a layover. Select 'All' to show
     	data for predictions, 'Affected by layover' to only see data where
     	predictions affected by when a driver is scheduled to leave a layover,
     	or 'Not affected by layover' if you only want data for predictions
     	that were not affected by layovers.">
      <option value="">All</option>
      <option value="AffectedByWaitStop">Affected by layover</option>
      <option value="NotAffectedByWaitStop">Not affected by layover</option>
    </select>
  </div>
</div>
