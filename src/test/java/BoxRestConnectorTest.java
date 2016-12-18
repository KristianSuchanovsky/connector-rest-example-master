import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;



import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.junit.Test;

import com.evolveum.polygon.connector.example.rest.BoxRestConnector;


public class BoxRestConnectorTest {
	
	private static final Log LOG = Log.getLog(BoxRestConnector.class);
	 public static SearchResultsHandler handler = new SearchResultsHandler() {

		  @Override
		  public boolean handle(ConnectorObject connectorObject) {
		   results.add(connectorObject);
		   return true;
		  }

		@Override
		public void handleResult(SearchResult result) {
			 LOG.info("im handling {0}", result.getRemainingPagedResults());
			
		}
		 };
		 private static final ArrayList<ConnectorObject> results = new ArrayList<>();
	@Test
		public void aFastTest()  {
		BoxRestConnector connector = new BoxRestConnector();
		ObjectClass objectClass = new ObjectClass("__ACCOUNT__");
		Set<Attribute> attributes = new HashSet<Attribute>();
		attributes.add(AttributeBuilder.build("login","jonsnow@westeros.com"));
		attributes.add(AttributeBuilder.build("name", "Jon Snow"));
		//LOGGER.ok("CreateOUT: {0}", connector.create(objectClass, attributes, null).toString());
		
		//AttributeFilter swfilter = (StartsWithFilter) FilterBuilder
		//									.startsWith(AttributeBuilder.build("__NAME__", "l"));
		
		AttributeFilter eqfilter = (EqualsFilter) FilterBuilder
											.equalTo(AttributeBuilder.build("__UID__", "508621369"));
		
		
		
		connector.executeQuery(objectClass, eqfilter, handler, null);
		//connector.executeQuery(objectClass, eqfilter, handler, null);
		
		//Uid	uid = new Uid("502946796");
		
		/*attributes.add(AttributeBuilder.build("name", "RENLEY Baratheon"));
		LOG.ok("UpdateOUT: {0}", connector.update(objectClass, uid, attributes, null).toString());*/
		
		//connector.delete(objectClass, uid, null);
		
		  }
	

}
