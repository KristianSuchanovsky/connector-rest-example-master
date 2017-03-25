import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;



import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
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

import com.evolveum.polygon.connector.example.rest.BoxConnectorConfiguration;
import com.evolveum.polygon.connector.example.rest.BoxConnector;


public class BoxRestConnectorTest {
	
	private static final Log LOG = Log.getLog(BoxConnector.class);
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
		 BoxConnector connector = new BoxConnector();
		 BoxConnectorConfiguration conf =new BoxConnectorConfiguration();
		
		 GuardedString clientSecret = new GuardedString(new String("L9SzKgeLU28jFzmmhYEAabqxEWTmcDT4").toCharArray());
		 GuardedString refreshToken = new GuardedString(new String("Q0N0gAmd1xaXMNuFQHiCPEhQUIlhpdvbPzrRXK3RC8unrXwVsQSMts1l4KwgdCo5").toCharArray());
	
		 /*ObjectClass object = new ObjectClass("Collaborations");
		 Set<Attribute> attributes = new HashSet<Attribute>();
		 attributes.add(AttributeBuilder.build("item.id","22025286583"));
		 attributes.add(AttributeBuilder.build("item.type","folder"));
		 attributes.add(AttributeBuilder.build("accessible_by.id","1311377537"));
		 attributes.add(AttributeBuilder.build("accessible_by.type","user"));
		 attributes.add(AttributeBuilder.build("role", "viewer"));*/
		 
		 /*ObjectClass object = new ObjectClass("Folders");
		 Set<Attribute> attributes = new HashSet<Attribute>();
		 attributes.add(AttributeBuilder.build("parent.id","22218821104"));
		 attributes.add(AttributeBuilder.build("__NAME__", "ESET"));*/
		 
		 /*ObjectClass object = new ObjectClass("__ACCOUNT__");
		 Set<Attribute> attributes = new HashSet<Attribute>();
		 attributes.add(AttributeBuilder.build("login","Matus@evo.com"));
		 attributes.add(AttributeBuilder.build("__NAME__", "Matus Evolveum"));*/
		 
		 /*ObjectClass object = new ObjectClass("__GROUP__");
		 Set<Attribute> attributes = new HashSet<Attribute>();
		 attributes.add(AttributeBuilder.build("__NAME__", "EastcubatorGroup"));*/
		 
		 ObjectClass object = new ObjectClass("Membership");
		 Set<Attribute> attributes = new HashSet<Attribute>();
		 attributes.add(AttributeBuilder.build("user.id", "1311377537"));
		 attributes.add(AttributeBuilder.build("group.id", "143246148"));
		 attributes.add(AttributeBuilder.build("role", "admin"));
		 
		
		 conf.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		 conf.setClientSecret(clientSecret);
		 conf.setRefreshToken(refreshToken);
		 conf.setUri("api.box.com");
		 
		 //connector.init(conf);
		 //connector.test();
		 // folder Ceresnicka Id 21731126767
		 // folder Eastcubator Id 21735961057
		 // user Lukas Evolveum Id 1288909013
		 // user Matus Evolveum Id 1311377537
		 // collab Lukas - Eastcoubator Id 3708317822
		 // group EastcubatorGroup Id 143246148
		 // folder 21.03.2017 Id 22025286583
		 // folder 23.03.2017 Id 22218663533
		 // folder BOX Id 22218774509
		 // folder FOLDER Id 22218821104
		 // folder ESET Id 22218922123
		 // collab Matus - 21.03.2017 Id 3793668509
		 // member Matus - EastcubatorGroup Id 1163197315
		 //Uid uid = new Uid("1163197315");
		 //connector.update(object,uid , attributes, null);
		 //connector.create(object, attributes, null);
		 //connector.delete(object, uid, null);
		 
		/*AttributeFilter swfilter = (StartsWithFilter) FilterBuilder.startsWith(AttributeBuilder.build("__NAME__", "E"));
		//connector.executeQuery(object, swfilter, handler, null);*/
		//connector.executeQuery(object, null, handler, null);

		/*AttributeFilter eqfilter = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build("__UID__", " "));
		connector.executeQuery(object, eqfilter, handler, null);*/
		
		  }
	

}
