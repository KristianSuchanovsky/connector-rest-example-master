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
import org.identityconnectors.framework.common.objects.AttributeBuilder;
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
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.identityconnectors.framework.impl.api.local.operations.FilteredResultsHandler;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserHandler extends ObjectsProcessing {
	private static final Log LOGGER = Log.getLog(UserHandler.class);

	public UserHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final String ATTR_LOGIN = "login";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_ROLE = "role";
	private static final String ATTR_LANGUAGE = "language";
	private static final String ATTR_SYNC = "is_sync_enabled";
	private static final String ATTR_TITLE = "job_title";
	private static final String ATTR_PHONE = "phone";
	private static final String ATTR_ADDRESS = "address";
	private static final String ATTR_SPACE = "space_amount";
	private static final String ATTR_MANAGED = "can_see_managed_users";
	private static final String ATTR_TIMEZONE = "timezone";
	private static final String ATTR_DEVICELIMITS = "is_exempt_from_device_limits";
	private static final String ATTR_LOGINVERIFICATION = "is_exempt_from_login_verification";
	private static final String ATTR_COLLAB = "is_external_collab_restricted";
	private static final String ATTR_STATUS = "status";
	private static final String ATTR_AVATAR = "avatar_url";
	private static final String ATTR_ENTERPRISE = "enterprise";
	private static final String ATTR_NOTIFY = "notify";
	private static final String ATTR_CREATED = "created_at";
	private static final String ATTR_MODIFIED = "modified_at";
	private static final String ATTR_USED = "space_used";
	private static final String ATTR_PSSWD = "is_password_reset_required";
	private static final String ATTR_CODE = "tracking_codes";

	private static final String FILTERTERM = "filter_term";
	private static final String OFFSET = "offset";
	private static final String LIMIT = "limit";
	private static final String CRUD = "/2.0/users";
	private static final String UID = "id";
	private static final String AVATAR = "avatar_url";

	public ObjectClassInfo getUserSchema() {

		ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
		// mail
		AttributeInfoBuilder attrLoginBuilder = new AttributeInfoBuilder(ATTR_LOGIN);
		attrLoginBuilder.setRequired(true);
		ocBuilder.addAttributeInfo(attrLoginBuilder.build());
		// name
		AttributeInfoBuilder attrNameBuilder = new AttributeInfoBuilder(ATTR_NAME);
		attrNameBuilder.setRequired(true);
		ocBuilder.addAttributeInfo(attrNameBuilder.build());
		// role
		AttributeInfoBuilder attrRoleBuilder = new AttributeInfoBuilder(ATTR_ROLE);
		ocBuilder.addAttributeInfo(attrRoleBuilder.build());
		// language
		AttributeInfoBuilder attrLanguageBuilder = new AttributeInfoBuilder(ATTR_LANGUAGE);
		ocBuilder.addAttributeInfo(attrLanguageBuilder.build());
		// is_sync_enabled
		AttributeInfoBuilder attrIsSyncEnabledBuilder = new AttributeInfoBuilder(ATTR_SYNC, Boolean.class);
		ocBuilder.addAttributeInfo(attrIsSyncEnabledBuilder.build());
		// job_titile
		AttributeInfoBuilder attrJobTitleBuilder = new AttributeInfoBuilder(ATTR_TITLE);
		ocBuilder.addAttributeInfo(attrJobTitleBuilder.build());
		// phone
		AttributeInfoBuilder attrPhoneBuilder = new AttributeInfoBuilder(ATTR_PHONE);
		ocBuilder.addAttributeInfo(attrPhoneBuilder.build());
		// address
		AttributeInfoBuilder attrAddressBuilder = new AttributeInfoBuilder(ATTR_ADDRESS);
		ocBuilder.addAttributeInfo(attrAddressBuilder.build());
		// space_amount
		AttributeInfoBuilder attrSpaceAmountBuilder = new AttributeInfoBuilder(ATTR_SPACE);
		ocBuilder.addAttributeInfo(attrSpaceAmountBuilder.build());
		// can_see_managed_users
		AttributeInfoBuilder attrCanSeeManagedUsersBuilder = new AttributeInfoBuilder(ATTR_MANAGED, Boolean.class);
		ocBuilder.addAttributeInfo(attrCanSeeManagedUsersBuilder.build());
		// timezone
		AttributeInfoBuilder attrTimezoneBuilder = new AttributeInfoBuilder(ATTR_TIMEZONE);
		ocBuilder.addAttributeInfo(attrTimezoneBuilder.build());
		// is_exempt_from_device_limits
		AttributeInfoBuilder attrIsExemptFromDeviceLimits = new AttributeInfoBuilder(ATTR_DEVICELIMITS, Boolean.class);
		ocBuilder.addAttributeInfo(attrIsExemptFromDeviceLimits.build());
		// is_exempt_from_login_verification
		AttributeInfoBuilder attrIsExemptFromLoginVerification = new AttributeInfoBuilder(ATTR_LOGINVERIFICATION,
				Boolean.class);
		ocBuilder.addAttributeInfo(attrIsExemptFromLoginVerification.build());
		// avatar
		AttributeInfoBuilder attrAvatar = new AttributeInfoBuilder(ATTR_AVATAR, byte[].class);
		ocBuilder.addAttributeInfo(attrAvatar.build());
		// is_external_collab_restricted
		AttributeInfoBuilder attrCollab = new AttributeInfoBuilder(ATTR_COLLAB, Boolean.class);
		ocBuilder.addAttributeInfo(attrCollab.build());
		// status
		AttributeInfoBuilder attrStatus = new AttributeInfoBuilder(ATTR_STATUS);
		ocBuilder.addAttributeInfo(attrStatus.build());
		// enterprise
		AttributeInfoBuilder attrEnterpise = new AttributeInfoBuilder(ATTR_ENTERPRISE);
		ocBuilder.addAttributeInfo(attrEnterpise.build());
		// notify
		AttributeInfoBuilder attrNotify = new AttributeInfoBuilder(ATTR_NOTIFY, Boolean.class);
		ocBuilder.addAttributeInfo(attrNotify.build());

		AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
		ocBuilder.addAttributeInfo(attrCreated.build());

		AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
		ocBuilder.addAttributeInfo(attrModified.build());

		AttributeInfoBuilder attrUsed = new AttributeInfoBuilder(ATTR_USED, Integer.class);
		ocBuilder.addAttributeInfo(attrUsed.build());

		AttributeInfoBuilder attrPsswd = new AttributeInfoBuilder(ATTR_PSSWD, Boolean.class);
		ocBuilder.addAttributeInfo(attrPsswd.build());

		ObjectClassInfo userSchemaInfo = ocBuilder.build();
		LOGGER.info("The constructed User core schema: {0}", userSchemaInfo);
		return userSchemaInfo;
	}

	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler,
			OperationOptions options) {
		Name name = null;
		Uid uid = null;
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		int pageNumber = 0;
		int usersPerPage = 0;
		int offset = 0;
		if(!handler.getClass().getSimpleName().toString().equals("FilteredResultsHandler")){
			
		
		LOGGER.info("ResultHandler {0} ", handler.getClass().getSimpleName().toString());
		
		
		if (query instanceof StartsWithFilter && ((StartsWithFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();

			if (name != null) {
				uriBuilder.setPath(CRUD);
				uriBuilder.addParameter(FILTERTERM, name.getNameValue());
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
				
				handleUsers(request, handler, query);

			}
		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
			uid = (Uid) ((EqualsFilter) query).getAttribute();
			if (uid != null) {
				uriBuilder.setPath(CRUD + "/" + uid.getUidValue().toString());

				try {
					uri = uriBuilder.build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
							.append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				JSONObject user = callRequest(request, true);
				ConnectorObject connectorObject = convertToConnectorObject(user);
				handler.handle(connectorObject);

			}
		} else if (query instanceof ContainsFilter && ((ContainsFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();

			if (name != null) {
				uriBuilder.setPath(CRUD);
				uriBuilder.addParameter(FILTERTERM, name.getNameValue());
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
				handleUsers(request, handler, query);

			}
		} else if (query instanceof ContainsAllValuesFilter
				&& ((ContainsAllValuesFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();
			if (name != null) {
				uriBuilder.setPath(CRUD);
				uriBuilder.addParameter(FILTERTERM, name.getNameValue());
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
				handleUsers(request, handler, query);

			}
		} else if (query == null && objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			uriBuilder.setPath(CRUD);
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
			handleUsers(request, handler, query);
		}
		} else {
		LOGGER.info("YOU", "JOU");
		uriBuilder.setPath(CRUD);
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		HttpGet request = new HttpGet(uri);
		handleUsers(request, handler, query);
		}
		
	}

	private boolean handleUsers(HttpGet request, ResultsHandler handler, Filter filter) {
		if (request == null) {
			LOGGER.error("Request value not provided {0} ", request);
			throw new InvalidAttributeValueException("Request value not provided");
		}
			

		JSONObject result = callRequest(request);
		JSONArray users = result.getJSONArray("entries");

		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			LOGGER.ok("response body Handle user: {0}", user);

			ConnectorObject connectorObject = convertToConnectorObject(user);
			
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return true;
			}

		}
		
		
		

		return false;
	}
	
	

	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
		LOGGER.ok("delete user, Uid: {0}", uid);
		HttpDelete request;
		URI uri = null;

		try {
			uri = getURIBuilder().setPath(CRUD + "/" + uid.getUidValue().toString()).build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		request = new HttpDelete(uri);
		callRequest(request, false);
	}

	public Uid createOrUpdateUser(Uid uid, Set<Attribute> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			LOGGER.error("Attributes not provided {0} ", attributes);
			return uid;
		}
		boolean create = uid == null;
		JSONObject json = new JSONObject();
		// required attribute e-mail
		String login = getStringAttr(attributes, ATTR_LOGIN);
		if (create && StringUtil.isBlank(login)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_LOGIN);
		}
		if (login != null) {
			json.put(ATTR_LOGIN, login);
		}
		// required attribute name
		String name = getStringAttr(attributes, "__NAME__");
		if (create && StringUtil.isBlank(name)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
		}
		if (name != null) {
			json.put(ATTR_NAME, name);
		}
		putFieldIfExists(attributes, ATTR_ROLE, json);
		putFieldIfExists(attributes, ATTR_LANGUAGE, json);
		putFieldIfExists(attributes, ATTR_SYNC, json);
		putFieldIfExists(attributes, ATTR_TITLE, json);
		putFieldIfExists(attributes, ATTR_PHONE, json);
		putFieldIfExists(attributes, ATTR_ADDRESS, json);
		putFieldIfExists(attributes, ATTR_SPACE, json);
		putFieldIfExists(attributes, ATTR_MANAGED, json);
		putFieldIfExists(attributes, ATTR_TIMEZONE, json);
		putFieldIfExists(attributes, ATTR_DEVICELIMITS, json);
		putFieldIfExists(attributes, ATTR_LOGINVERIFICATION, json);
		putFieldIfExists(attributes, ATTR_COLLAB, json);
		putFieldIfExists(attributes, ATTR_NOTIFY, json);
		putFieldIfExists(attributes, ATTR_ENTERPRISE, json);
		putFieldIfExists(attributes, ATTR_PSSWD, json);
		//
		putFieldIfExists(attributes, ATTR_CREATED, json);
		putFieldIfExists(attributes, ATTR_MODIFIED, json);
		putFieldIfExists(attributes, ATTR_STATUS, json);
		putFieldIfExists(attributes, ATTR_USED, json);
		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;
		if (create) {
			try {
				URIBuilder uriBuilder = getURIBuilder();
				uri = uriBuilder.setPath(CRUD).build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			request = new HttpPost(uri);
		} else {
			// update

			try {
				uri = getURIBuilder().setPath(CRUD + "/" + uid.getUidValue().toString()).build();
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
		if (json.has(ATTR_NAME)) {
			builder.setName(json.getString(ATTR_NAME));
		}

		getIfExists(json, ATTR_LOGIN, builder);
		getIfExists(json, ATTR_ADDRESS, builder);
		getIfExists(json, ATTR_CODE, builder);
		getIfExists(json, ATTR_DEVICELIMITS, builder);
		getIfExists(json, ATTR_LANGUAGE, builder);
		getIfExists(json, ATTR_LOGINVERIFICATION, builder);
		getIfExists(json, ATTR_MANAGED, builder);
		getIfExists(json, ATTR_PHONE, builder);
		getIfExists(json, ATTR_ROLE, builder);
		getIfExists(json, ATTR_SPACE, builder);
		getIfExists(json, ATTR_SYNC, builder);
		getIfExists(json, ATTR_TIMEZONE, builder);
		getIfExists(json, ATTR_TITLE, builder);
		getIfExists(json, ATTR_AVATAR, builder);
		getIfExists(json, ATTR_CREATED, builder);
		getIfExists(json, ATTR_MODIFIED, builder);
		getIfExists(json, ATTR_STATUS, builder);
		getIfExists(json, ATTR_USED, builder);

		getAvatarPhoto(json, builder, AVATAR);

		ConnectorObject connectorObject = builder.build();
		return connectorObject;
	}

}
