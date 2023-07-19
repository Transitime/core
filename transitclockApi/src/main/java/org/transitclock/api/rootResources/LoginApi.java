package org.transitclock.api.rootResources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.config.StringConfigValue;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
public class LoginApi {

    private static StringConfigValue sessionCookieNames =
            new StringConfigValue("transitclock.api.login.sessionCookieNames", "",
                    "Login session cookie names. Used to keep track of session and logout.");

    private String[] getSessionCookieNames(){
        return sessionCookieNames.getValue().split("[\\s,;]+");
    }

    private static StringConfigValue logOutURL =
            new StringConfigValue("transitclock.api.login.logoutRedirectUrl", "/",
                    "Url to redirect user to after logging out.");

    private static StringConfigValue logOutGrafanaURL =
            new StringConfigValue("transitclock.api.login.logOutGrafanaURL", "/",
                    "Url to redirect user to for grafana after logging out.");

    @Path("/logout")
    @GET
    public Response logout(@BeanParam StandardParameters stdParameters,
                           @QueryParam("grafana") boolean isGrafana){
        try {
            if(isGrafana){
                return stdParameters.logout(logOutGrafanaURL.getValue(), getSessionCookieNames());
            }
            return stdParameters.logout(logOutURL.getValue(), getSessionCookieNames());
        } catch (Exception e) {
            throw WebUtils.badRequestException(e);
        }
    }
}
