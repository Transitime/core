package TheTransitClock.transitclockTraccar;

import java.util.List;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.Position;
import io.swagger.client.model.User;


/**
 * @author Sean Óg Crudden
 * Test App that reads all device positions from a traccar server. 
 * Uses classes generated from swagger file provided with traccar.
 */
public class GetPositions 
{
	private static String email="admin";
	private static String password="admin";
	private static String baseUrl="http://127.0.0.1:8082/api";
	
    public static void main( String[] args )
    {        
        DefaultApi api=new DefaultApi();
        ApiClient client=new ApiClient();
        client.setBasePath(baseUrl);
        client.setUsername(email);
        client.setPassword(password);        
        api.setApiClient(client);                               
        try {
			User user=api.sessionPost(email, password);
			
			List<Position> results = api.positionsGet(null, null, null, user.getId());
			
			for(Position result:results)
			{
				System.out.println(result);
			}
			
		} catch (ApiException e) {
			
			e.printStackTrace();
		}
    }
}
