<%-- This page returns the Http response error message and nothing more.
     It is intended for when a page makes a request and if there is
     an error the calling page can simply display the error message
     without the usual ugle error page html. --%>
<%= request.getAttribute("javax.servlet.error.message") %>