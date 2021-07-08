<% 
// For creating a text parameter via jsp include directive.

// Get the parameters for this boolean parameter
String label = request.getParameter("label"); 
String name = request.getParameter("name"); 
String defaultStr = request.getParameter("default");
String tooltip = request.getParameter("tooltip");
%>
  <div class="param">
    <label for="<%= name %>"><%= label %>:</label>
    <input id="<%= name %>" 
    	name="<%= name %>" 
    	title="<%= tooltip %>" 
    	value="<%= defaultStr==null?"" : defaultStr %>" />
  </div>