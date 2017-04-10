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

public class FoldersHandler extends ObjectsProcessing {

	public FoldersHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(FoldersHandler.class);

	private static final String ATTR_NAME = "name";
	private static final String ATTR_PARENT_ID = "parent.id";
	private static final String ATTR_FOLDER_DESC = "description";
	private static final String ATTR_PARENT = "parent";
	private static final String FOLDER_NAME = "Folders";

	private static final String CRUD_FOLDER = "/2.0/folders";
	private static final String UID = "id";

	public ObjectClassInfo getFoldersSchema() {
		ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();

		builder.setType(FOLDER_NAME);

		AttributeInfoBuilder attrNameBuilder = new AttributeInfoBuilder(ATTR_NAME);
		attrNameBuilder.setRequired(true);
		builder.addAttributeInfo(attrNameBuilder.build());

		AttributeInfoBuilder attrParentBuilder = new AttributeInfoBuilder(ATTR_PARENT_ID);
		attrParentBuilder.setRequired(true);
		builder.addAttributeInfo(attrParentBuilder.build());

		AttributeInfoBuilder attrDesc = new AttributeInfoBuilder(ATTR_FOLDER_DESC);
		builder.addAttributeInfo(attrDesc.build());

		ObjectClassInfo foldersSchemaInfo = builder.build();
		LOGGER.info("The constructed folders schema representation: {0}", foldersSchemaInfo);
		return foldersSchemaInfo;

	}

	public void executeCollabQuery(ObjectClass objectClass, Filter query, ResultsHandler handler,
			OperationOptions options) {
		Uid uid = null;
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		
		if (query instanceof StartsWithFilter) {

			StringBuilder sb = new StringBuilder();
			sb.append("Method not allowed: ").append((query).getClass().getSimpleName().toString()).append(";");
			throw new ConnectorException(sb.toString());

		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {

			uid = (Uid) ((EqualsFilter) query).getAttribute();
			if (uid != null) {
				uriBuilder.setPath(CRUD_FOLDER + "/" + uid.getUidValue().toString());
				try {
					uri = uriBuilder.build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
							.append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				JSONObject folder = callRequest(request, true);
				ConnectorObject connectorObject = convertToConnectorObject(folder);
				handler.handle(connectorObject);
			}

		} else if (query instanceof ContainsFilter) {
			String attrValue = ((ContainsFilter) query).getAttribute().getValue().get(0).toString();
			String attrName = getAttrName(query);
			uid = new Uid("0");
			substringFiltering(handler, options, attrName, attrValue, uid);

		} else if (query instanceof ContainsAllValuesFilter) {

			StringBuilder sb = new StringBuilder();
			sb.append("Method not allowed: ").append((query).getClass().getSimpleName().toString()).append(";");
			throw new ConnectorException(sb.toString());

		} else if (query == null && objectClass.is(FOLDER_NAME)) {
			handlerJson(handler, new Uid("0"));

		}
	}

	private void handlerJson(ResultsHandler handler, Uid uid) {
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		uriBuilder.setPath(CRUD_FOLDER + "/" + uid.getUidValue().toString());
		try {
			uri = uriBuilder.build();

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		HttpGet request = new HttpGet(uri);
		JSONObject result = callRequest(request, true);
		if (result.has("item_collection")) {
			JSONObject collection = result.getJSONObject("item_collection");
			JSONArray array = collection.getJSONArray("entries");
			for (int i = 0; i < array.length(); i++) {
				JSONObject folder = array.getJSONObject(i);
				handlerJson(handler, new Uid(String.valueOf(folder.get("id"))));
				ConnectorObject connectorObject = convertToConnectorObject(folder);
				handler.handle(connectorObject);

			}
		} else {
			ConnectorObject connectorObject = convertToConnectorObject(result);
			handler.handle(connectorObject);

		}
	}

	private boolean substringFiltering(ResultsHandler handler, OperationOptions options, String attrName,
			String subValue, Uid uid) {
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		uriBuilder.setPath(CRUD_FOLDER + "/" + uid.getUidValue().toString());
		try {
			uri = uriBuilder.build();

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		HttpGet request = new HttpGet(uri);
		JSONObject result = callRequest(request, true);
		if (result.has("item_collection")) {
			JSONObject collection = result.getJSONObject("item_collection");
			JSONArray array = collection.getJSONArray("entries");
			for (int i = 0; i < array.length(); i++) {
				JSONObject folder = array.getJSONObject(i);
				substringFiltering(handler, options, attrName, subValue, new Uid(String.valueOf(folder.get("id"))));
				if (!folder.has(attrName)) {
					LOGGER.warn("\n\tProcessing JSON Object does not contain attribute {0}.", attrName);
					return false;
				}

				if (folder.has(attrName) && (folder.get(attrName)).toString().contains(subValue)) {

					ConnectorObject connectorObject = convertToConnectorObject(folder);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return true;
					}
				}

			}
			return false;
		} else {
			if (!result.has(attrName)) {
				LOGGER.warn("\n\tProcessing JSON Object does not contain attribute {0}.", attrName);
				return false;
			}
			if (result.has(attrName) && (result.get(attrName)).toString().contains(subValue)) {
				ConnectorObject connectorObject = convertToConnectorObject(result);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return true;
				}
			}

		}
		return false;
	}

	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
		HttpDelete request;
		URI folderUri = null;

		try {
			folderUri = getURIBuilder().setPath(CRUD_FOLDER + "/" + uid.getUidValue().toString()).build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		request = new HttpDelete(folderUri);
		callRequest(request, false);
	}

	public Uid createOrUpdateFolder(Uid fid, Set<Attribute> attributes) {

		if (attributes == null || attributes.isEmpty()) {
			LOGGER.error("Attributes not provided {0} ", attributes);
			return fid;
		}

		boolean create = fid == null;
		JSONObject json = new JSONObject();
		JSONObject jsonParent = new JSONObject();

		// required attribute name
		String name = getStringAttr(attributes, "__NAME__");
		if (create && StringUtil.isBlank(name)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
		}
		if (name != null) {
			json.put(ATTR_NAME, name);
		}
		// required attribute parent Id
		String parentId = getStringAttr(attributes, ATTR_PARENT_ID);
		if (create && StringUtil.isBlank(parentId))
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_PARENT_ID);
		if (parentId != null) {
			// add json with required attribute id to parent json
			jsonParent.put("id", parentId);
			json.put(ATTR_PARENT, jsonParent);
		}

		putFieldIfExists(attributes, ATTR_FOLDER_DESC, json);

		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;
		if (create) {
			try {
				uri = getURIBuilder().setPath(CRUD_FOLDER).build();
				LOGGER.info("URI {0}", uri);
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			request = new HttpPost(uri);

		} else {
			try {
				uri = getURIBuilder().setPath(CRUD_FOLDER + "/" + fid.getUidValue().toString()).build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}

			request = new HttpPut(uri);
		}
		JSONObject jsonReq = callRequest(request, json);

		String newFid = jsonReq.getString(UID);
		LOGGER.ok("UID {0}", newFid);

		return new Uid(newFid);

	}

	public ConnectorObject convertToConnectorObject(JSONObject json) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

		builder.setUid(new Uid(json.getString(UID)));
		builder.setObjectClass(new ObjectClass(FOLDER_NAME));
		if (json.has(ATTR_NAME)) {
			builder.setName(json.getString(ATTR_NAME));
		}
		getIfExists(json, ATTR_FOLDER_DESC, builder);

		ConnectorObject connectorObject = builder.build();
		LOGGER.ok("convertUserToConnectorObject,\n\tconnectorObject: {0}", connectorObject);
		return connectorObject;
	}

	private String getAttrName(Filter query) {
		if (((ContainsFilter) query).getAttribute() instanceof Name) {
			return "name";
		} else
			return ((ContainsFilter) query).getAttribute().getName();

	}

}
