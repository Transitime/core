<%@ page import="org.transitclock.utils.web.WebUtils" %>
<div class="row param" <% if(WebUtils.showApiKey()){ out.print("style=\"display:none\""); } %>>
  <div class="col-sm-5 label">API Key:</div>
  <div class="col-sm-7">
    <input type="text" id="apiKey" value="<%= WebUtils.getShowableApiKey() %>" name="apiKey" required />
    <div class="invalid-feedback">Please enter an API key.</div>
  </div>
</div>
