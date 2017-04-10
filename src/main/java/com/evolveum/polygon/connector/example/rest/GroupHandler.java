package com.evolveum.polygon.connector.example.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class GroupHandler extends ObjectsProcessing {

	public GroupHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(GroupHandler.class);

	private static final String ATTR_NAME = "name";
	private static final String ATTR_PROVENANCE = "provenance";
	private static final String ATTR_IDENTIFIER = "external_sync_identifier";
	private static final String ATTR_DESCRIPTION = "description";
	private static final String ATTR_INVITABILITY = "invitability_level";
	private static final String ATTR_VIEWABILITY = "member_viewability_level";
	private static final String ATTR_CREATED = "created_at";
	private static final String ATTR_MODIFIED = "modified_at";
	private static final String ATTR_SYNC = "is_sync_enabled";

	private static final String CRUD_GROUP = "/2.0/groups";
	private static final String OFFSET = "offset";
	private static final String LIMIT = "limit";
	private static final String UID = "id";

	public ObjectClassInfo getGroupSchema() {
		ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();

		builder.setType(ObjectClass.GROUP_NAME);

		AttributeInfoBuilder attrNameBuilder = new AttributeInfoBuilder(ATTR_NAME);
		attrNameBuilder.setRequired(true);
		builder.addAttributeInfo(attrNameBuilder.build());

		AttributeInfoBuilder attrProvenanceBuilder = new AttributeInfoBuilder(ATTR_PROVENANCE);
		builder.addAttributeInfo(attrProvenanceBuilder.build());

		AttributeInfoBuilder attrIdentifierBuilder = new AttributeInfoBuilder(ATTR_IDENTIFIER);
		builder.addAttributeInfo(attrIdentifierBuilder.build());

		AttributeInfoBuilder attrDescriptionBuilder = new AttributeInfoBuilder(ATTR_DESCRIPTION);
		builder.addAttributeInfo(attrDescriptionBuilder.build());

		AttributeInfoBuilder attrInvitabilityBuilder = new AttributeInfoBuilder(ATTR_INVITABILITY);
		builder.addAttributeInfo(attrInvitabilityBuilder.build());

		AttributeInfoBuilder attrViewabilityBuilder = new AttributeInfoBuilder(ATTR_VIEWABILITY);
		builder.addAttributeInfo(attrViewabilityBuilder.build());

		AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
		builder.addAttributeInfo(attrCreated.build());

		AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
		builder.addAttributeInfo(attrModified.build());

		ObjectClassInfo groupSchemaInfo = builder.build();
		LOGGER.info("The constructed group schema representation: {0}", groupSchemaInfo);
		return groupSchemaInfo;
	}

	public void executeCollabQuery(ObjectClass objectClass, Filter query, ResultsHandler handler,
			OperationOptions options) {
		Uid uid = null;
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		int pageNumber = 0;
		int usersPerPage = 0;
		int offset = 0;

		if (query instanceof StartsWithFilter) {

			StringBuilder sb = new StringBuilder();
			sb.append("Method not allowed: ").append((query).getClass().getSimpleName().toString()).append(";");
			throw new ConnectorException(sb.toString());

		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
			uid = (Uid) ((EqualsFilter) query).getAttribute();
			if (uid != null) {
				uriBuilder.setPath(CRUD_GROUP + "/" + uid.getUidValue().toString());
				try {
					uri = uriBuilder.build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
							.append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				JSONObject group = callRequest(request, true);
				ConnectorObject connectorObject = convertToConnectorObject(group);
				handler.handle(connectorObject);

			}

		} else if (query instanceof ContainsFilter) {
			String attrValue = ((ContainsFilter) query).getAttribute().getValue().get(0).toString();
			String attrName = getAttrName(query);
			if (attrValue != null) {

				uriBuilder.setPath(CRUD_GROUP);
				if (options != null) {
					if ((options.getPageSize()) != null) {
						usersPerPage = options.getPageSize();
						uriBuilder.addParameter(LIMIT, String.valueOf(usersPerPage));
						if (options.getPagedResultsOffset() != null) {
							pageNumber = options.getPagedResultsOffset();
							offset = (pageNumber * usersPerPage) - usersPerPage;
							uriBuilder.addParameter(OFFSET, String.valueOf(offset));
						}
					}
				}
				try {
					uri = uriBuilder.build();
				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
							.append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				substringFiltering(request, handler, options, attrName, attrValue);
			}
		} else if (query instanceof ContainsAllValuesFilter) {

			StringBuilder sb = new StringBuilder();
			sb.append("Method not allowed: ").append((query).getClass().getSimpleName().toString()).append(";");
			throw new ConnectorException(sb.toString());

		} else if (query == null && objectClass.is(ObjectClass.GROUP_NAME)) {

			uriBuilder.setPath(CRUD_GROUP);
			if (options != null) {
				if ((options.getPageSize()) != null) {
					usersPerPage = options.getPageSize();
					uriBuilder.addParameter(LIMIT, String.valueOf(usersPerPage));
					if (options.getPagedResultsOffset() != null) {
						pageNumber = options.getPagedResultsOffset();
						offset = (pageNumber * usersPerPage) - usersPerPage;
						uriBuilder.addParameter(OFFSET, String.valueOf(offset));
					}
				}
			}
			try {
				uri = uriBuilder.build();

			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			HttpGet request = new HttpGet(uri);
			handleGroups(request, handler, options, CRUD_GROUP);
		}
	}

	private boolean handleGroups(HttpGet request, ResultsHandler handler, OperationOptions options, String object) {
		if (request == null) {
			LOGGER.error("Request value not provided {0} ", request);
			throw new InvalidAttributeValueException("Request value not provided");
		}

		JSONObject result = callRequest(request);
		LOGGER.info("JSON {0}", result);
		JSONArray groups = result.getJSONArray("entries");

		for (int i = 0; i < groups.length(); i++) {
			JSONObject group = groups.getJSONObject(i);
			LOGGER.ok("response body Handle user: {0}", group);

			ConnectorObject connectorObject = convertToConnectorObject(group);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return true;
			}

		}

		return false;
	}

	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
		HttpDelete request;
		URI groupUri = null;

		try {
			groupUri = getURIBuilder().setPath(CRUD_GROUP + "/" + uid.getUidValue().toString()).build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		request = new HttpDelete(groupUri);
		callRequest(request, false);
	}

	public Uid createOrUpdateGroup(Uid uid, Set<Attribute> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			LOGGER.error("Attributes not provided {0} ", attributes);
			return uid;
		}

		boolean create = uid == null;
		JSONObject json = new JSONObject();

		// required attribute name
		String name = getStringAttr(attributes, "__NAME__");
		if (create && StringUtil.isBlank(name)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
		}
		if (name != null) {
			json.put(ATTR_NAME, name);
		}

		putFieldIfExists(attributes, ATTR_PROVENANCE, json);
		putFieldIfExists(attributes, ATTR_IDENTIFIER, json);
		putFieldIfExists(attributes, ATTR_DESCRIPTION, json);
		putFieldIfExists(attributes, ATTR_INVITABILITY, json);
		putFieldIfExists(attributes, ATTR_VIEWABILITY, json);

		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;
		if (create) {
			try {
				uri = getURIBuilder().setPath(CRUD_GROUP).build();
				LOGGER.info("URI", uri);
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			request = new HttpPost(uri);

		} else {
			try {
				uri = getURIBuilder().setPath(CRUD_GROUP + "/" + uid.getUidValue().toString()).build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}

			request = new HttpPut(uri);
		}
		JSONObject jsonReq = callRequest(request, json);

		String newUid = jsonReq.getString(UID);
		LOGGER.ok("UID {0}", newUid);

		return new Uid(newUid);
	}

	public ConnectorObject convertToConnectorObject(JSONObject json) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

		builder.setUid(new Uid(json.getString(UID)));
		builder.setObjectClass(new ObjectClass("__GROUP__"));
		if (json.has(ATTR_NAME)) {
			builder.setName(json.getString(ATTR_NAME));
		}
		getIfExists(json, ATTR_PROVENANCE, builder);
		getIfExists(json, ATTR_DESCRIPTION, builder);
		getIfExists(json, ATTR_SYNC, builder);
		getIfExists(json, ATTR_INVITABILITY, builder);
		getIfExists(json, ATTR_VIEWABILITY, builder);

		ConnectorObject connectorObject = builder.build();
		LOGGER.ok("convertUserToConnectorObject,\n\tconnectorObject: {0}", connectorObject);
		return connectorObject;
	}

	private boolean substringFiltering(HttpGet request, ResultsHandler handler, OperationOptions options,
			String attrName, String subValue) {
		if (request == null) {
			LOGGER.error("Request value not provided {0} ", request);
			throw new InvalidAttributeValueException("Request value not provided");
		}
		JSONObject result = callRequest(request);

		JSONArray members = result.getJSONArray("entries");
		// String attrName = attribute.getName().toString();
		// LOGGER.info("\n\tSubstring filtering: {0} ({1})", attrName,
		// subValue);
		for (int i = 0; i < members.length(); i++) {
			JSONObject member = members.getJSONObject(i);
			if (!member.has(attrName)) {
				LOGGER.warn("\n\tProcessing JSON Object does not contain attribute {0}.", attrName);
				return false;
			}
			if (member.has(attrName) && (member.get(attrName)).toString().contains(subValue)) {
				// LOG.ok("value: {0}, subValue: {1} - MATCH: {2}",
				// jsonObject.get(attrName).toString(), subValue, "YES");
				ConnectorObject connectorObject = convertToConnectorObject(member);
				;
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return true;
				}
			}
			// else LOG.ok("value: {0}, subValue: {1} - MATCH: {2}",
			// jsonObject.getString(attrName), subValue, "NO");
		}
		return false;
	}

	private String getAttrName(Filter query) {
		if (((ContainsFilter) query).getAttribute() instanceof Name) {
			return "name";
		} else
			return ((ContainsFilter) query).getAttribute().getName();

	}

}
