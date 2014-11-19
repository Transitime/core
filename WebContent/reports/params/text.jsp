<% 
// For creating a boolean parameter via jsp include directive.

// Get the parameters for this boolean parameter
String label = request.getParameter("label"); 
String name = request.getParameter("name"); 
String defaultStr = request.getParameter("default");
%>
  <div class="param">
    <label for="<%= name %>"><%= label %>:</label>
    <input id="<%= name %>" name="<%= name %>" value="<%= defaultStr %>" />
  </div>