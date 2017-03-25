package com.evolveum.polygon.connector.example.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

public class MembershipHandler extends ObjectsProcessing {

	

	public MembershipHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(GroupHandler.class);

	private static final String MEMBERSHIP_NAME = "Membership";
	private static final String ATTR_USER_ID = "user.id";
	private static final String ATTR_USER_NAME = "user.name";
	private static final String ATTR_USER_LOGIN = "user.login";
	private static final String ATTR_GROUP_NAME = "name";
	private static final String ATTR_GROUP_ID = "group.id";
	private static final String ATTR_ROLE = "role";
	private static final String ATTR_USER = "user";
	private static final String ATTR_GROUP = "group";

	private static final String FILTERTERM = "filter_term";
	private static final String CRUD_MEMBER = "/2.0/group_memberships";
	private static final String UID = "id";

	public ObjectClassInfo getMembershipSchema() {
		ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();

		builder.setType(MEMBERSHIP_NAME);

		AttributeInfoBuilder attrUserIdBuilder = new AttributeInfoBuilder(ATTR_USER_ID);
		attrUserIdBuilder.setRequired(true);
		builder.addAttributeInfo(attrUserIdBuilder.build());

		AttributeInfoBuilder attrGroupIdBuilder = new AttributeInfoBuilder(ATTR_GROUP_ID);
		attrGroupIdBuilder.setRequired(true);
		builder.addAttributeInfo(attrGroupIdBuilder.build());

		AttributeInfoBuilder attrRoleBuilder = new AttributeInfoBuilder(ATTR_ROLE);
		attrRoleBuilder.setRequired(true);
		builder.addAttributeInfo(attrRoleBuilder.build());

		AttributeInfoBuilder attrUserName = new AttributeInfoBuilder(ATTR_USER_NAME);
		builder.addAttributeInfo(attrUserName.build());

		AttributeInfoBuilder attrUserLogin = new AttributeInfoBuilder(ATTR_USER_LOGIN);
		builder.addAttributeInfo(attrUserLogin.build());

		AttributeInfoBuilder attrGroupName = new AttributeInfoBuilder(ATTR_GROUP_NAME);
		builder.addAttributeInfo(attrGroupName.build());

		ObjectClassInfo collaborationSchemaInfo = builder.build();
		LOGGER.info("The constructed membership schema representation: {0}", collaborationSchemaInfo);
		return collaborationSchemaInfo;
	}

	public void executeCollabQuery(ObjectClass objectClass, Filter query, ResultsHandler handler,
			OperationOptions options) {
		Name name = null;
		Uid uid = null;
		URI uri = null;

		if (query instanceof StartsWithFilter && ((StartsWithFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();

			if (name != null) {
				try {
					uri = getURIBuilder().setPath(CRUD_MEMBER).addParameter(FILTERTERM, name.getNameValue())
							.build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:")
							.append(getURIBuilder().toString()).append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				handleMembers(request, handler, options, CRUD_MEMBER);
			}

		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
			uid = (Uid) ((EqualsFilter) query).getAttribute();
			if (uid != null) {
				try {
					uri = getURIBuilder().setPath(CRUD_MEMBER + "/" + uid.getUidValue().toString()).build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:")
							.append(getURIBuilder().toString()).append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				JSONObject member = callRequest(request, true);
				ConnectorObject connectorObject = convertToConnectorObject(member);
				handler.handle(connectorObject);
			}

		} else if (query instanceof ContainsFilter && ((ContainsFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();
			if (name != null) {
				try {
					uri = getURIBuilder().setPath(CRUD_MEMBER).addParameter(FILTERTERM, name.getNameValue())
							.build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:")
							.append(getURIBuilder().toString()).append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				handleMembers(request, handler, options, CRUD_MEMBER);
			}

		} else if (query instanceof ContainsAllValuesFilter
				&& ((ContainsAllValuesFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();
			if (name != null) {
				try {
					uri = getURIBuilder().setPath(CRUD_MEMBER).addParameter(FILTERTERM, name.getNameValue())
							.build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:")
							.append(getURIBuilder().toString()).append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				handleMembers(request, handler, options, CRUD_MEMBER);
			}

		} else if (query == null && objectClass.is(MEMBERSHIP_NAME)) {
			try {
				uri = getURIBuilder().setPath(CRUD_MEMBER).build();

			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:")
						.append(getURIBuilder().toString()).append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			HttpGet request = new HttpGet(uri);
			handleMembers(request, handler, options, CRUD_MEMBER);
		}

	}

	private boolean handleMembers(HttpGet request, ResultsHandler handler, OperationOptions options,
			String object) {
		if (request == null) {
			LOGGER.error("Request value not provided {0} ", request);
			throw new InvalidAttributeValueException("Request value not provided");
		}

		JSONObject result = callRequest(request);
		LOGGER.info("JSON {0}", result);
		JSONArray members = result.getJSONArray("entries");

		for (int i = 0; i < members.length(); i++) {
			JSONObject member = members.getJSONObject(i);
			LOGGER.ok("response body Handle user: {0}", member);

			ConnectorObject connectorObject = convertToConnectorObject(member);;
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return true;
			}

		}

		return false;
	}

	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
		HttpDelete request;
		URI memberUri = null;

		try {
			memberUri = getURIBuilder().setPath(CRUD_MEMBER + "/" + uid.getUidValue().toString()).build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		request = new HttpDelete(memberUri);
		callRequest(request, false);
	}

	public Uid createOrUpdateMembership(Uid uid, Set<Attribute> attributes) {

		if (attributes == null || attributes.isEmpty()) {
			LOGGER.error("Attributes not provided {0} ", attributes);
			return uid;
		}

		boolean create = uid == null;
		JSONObject json = new JSONObject();
		JSONObject jsonUser = new JSONObject();
		JSONObject jsonGroup = new JSONObject();

		String userId = getStringAttr(attributes, ATTR_USER_ID);
		if (create && StringUtil.isBlank(userId)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_USER_ID);
		}
		if (userId != null) {
			jsonUser.put("id", userId);
		}

		String groupId = getStringAttr(attributes, ATTR_GROUP_ID);
		if (create && StringUtil.isBlank(groupId)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_GROUP_ID);
		}
		if (groupId != null) {
			jsonGroup.put("id", groupId);
		}

		String role = getStringAttr(attributes, ATTR_ROLE);
		if (create && StringUtil.isBlank(role)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_ROLE);
		}
		if (role != null) {
			json.put(ATTR_ROLE, role);
		}

		json.put(ATTR_USER, jsonUser);
		json.put(ATTR_GROUP, jsonGroup);

		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;

		if (create) {
			try {
				uri = getURIBuilder().setPath(CRUD_MEMBER).build();
				LOGGER.info("URI", uri);
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:")
						.append(getURIBuilder().toString()).append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			request = new HttpPost(uri);

		} else {
			try {
				uri = getURIBuilder().setPath(CRUD_MEMBER + "/" + uid.getUidValue().toString()).build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:")
						.append(getURIBuilder().toString()).append(";").append(e.getLocalizedMessage());
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

		JSONObject jsonUser = new JSONObject();
		JSONObject jsonGroup = new JSONObject();

		builder.setUid(new Uid(json.getString(UID)));
		builder.setObjectClass(new ObjectClass(MEMBERSHIP_NAME));
		if (json.has(ATTR_USER)) {
			if (json.get(ATTR_USER) != null && !JSONObject.NULL.equals(json.get(ATTR_USER))) {
				jsonUser = (JSONObject) json.get(ATTR_USER);
			}
		}

		if (json.has(ATTR_GROUP)) {
			if (json.get(ATTR_GROUP) != null && !JSONObject.NULL.equals(json.get(ATTR_GROUP))) {
				jsonGroup = (JSONObject) json.get(ATTR_GROUP);
			}
		}
		getIfExists(jsonUser, "id", builder);
		getIfExists(jsonUser, "name", builder);
		getIfExists(jsonUser, "login", builder);
		getIfExists(jsonGroup, "id", builder);
		getIfExists(jsonGroup, "name", builder);
		getIfExists(json, ATTR_ROLE, builder);

		ConnectorObject connectorObject = builder.build();
		LOGGER.ok("convertUserToConnectorObject,\n\tconnectorObject: {0}", connectorObject);
		return connectorObject;
	}
}
