<%@ page import="org.transitclock.reports.ReportsConfig" %>
<div class="param row"
<%
if(!ReportsConfig.isShowPredictionSource()) {
	out.print("style=\"display:none;\""); 
}
%>
>
	<div class="col-sm-5 label">Prediction Source:</div>
	<div class="col-sm-7">
		<select id="source"
				name="source"
				title="Specifies which prediction system to display data for. Selecting
     	'TransitClock' means will only show prediction data generated by TransitClock.
     	If there is another prediction source then can select 'Other'. And selecting 'All'
     	displays data for all prediction sources.">
			<option value="TransitClock">TransitClock</option>
			<option value="Other">Other</option>
			<option value="">All</option>
		</select>
	</div>
</div>