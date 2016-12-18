/**
 * Copyright (c) 2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.polygon.connector.example.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evolveum.polygon.rest.AbstractRestConnector;

import sun.security.jgss.LoginConfigImpl;

/**
 * @author semancik
 *
 */
@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = ExampleRestConfiguration.class)
public class BoxRestConnector extends AbstractRestConnector<ExampleRestConfiguration> implements TestOp, SchemaOp,
		Connector, DeleteOp, SearchOp<Filter>, UpdateOp, CreateOp, UpdateAttributeValuesOp {

	private static final Log LOG = Log.getLog(BoxRestConnector.class);
	// atrributes
	private static final String ATTR_LOGIN = "login";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_ROLE = "role";
	private static final String ATTR_LANGUAGE = "language";
	private static final String ATTR_SYNC = "is_sync_enabled";
	private static final String ATTR_TITLE = "job_title";
	private static final String ATTR_PHONE = "phone";
	private static final String ATTR_ADDRESS = "address";
	private static final String ATTR_SPACE = "space_amount";
	private static final String ATTR_CODE = "tracking_codes";
	private static final String ATTR_MANAGED = "can_see_managed_users";
	private static final String ATTR_TIMEZONE = "timezone";
	private static final String ATTR_DEVICELIMITS = "is_exempt_from_device_limits";
	private static final String ATTR_LOGINVERIFICATION = "is_exempt_from_login_verification";

	// JSON Header
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";

	private static final String USER = "/user";
	private static final String UID = "id";
	private static final String FILTERTERM = "filter_term";
	
	// Token
	private String refreshToken = "U9OQW2WKrUjkiunFdbIXm30Wup3m8HH9MT5ll4EigKPd4CIJMJlI1g2WqXVNzfZB";
	private static final String clientID = "4ig3tzk76msrvvvpradguxsxuz7lsuhr";
	private static final String clientSecret = "L9SzKgeLU28jFzmmhYEAabqxEWTmcDT4";
	private static final String grantType = "refresh_token";
	private String accessToken = "";
	private URIBuilder uri;

	public BoxRestConnector() {
		uri = new URIBuilder().setScheme("https").setHost("api.box.com/2.0/users/");

	}

	@Override
	public URIBuilder getURIBuilder() {
		return this.uri;
	}

	@Override
	public void test() {
		URIBuilder uriBuilder = getURIBuilder();
		URI uri;
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		HttpGet request = new HttpGet(uri);

		CloseableHttpResponse response = execute(request);

		processResponseErrors(response);
	}

	@Override
	public Schema schema() {
		SchemaBuilder schemaBuilder = new SchemaBuilder(BoxRestConnector.class);
		ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();

		// mail
		AttributeInfoBuilder attrLoginBuilder = new AttributeInfoBuilder(ATTR_LOGIN);
		attrLoginBuilder.setRequired(true);
		ocBuilder.addAttributeInfo(attrLoginBuilder.build());
		// name
		AttributeInfoBuilder attrNameBuilder = new AttributeInfoBuilder(ATTR_NAME);
		attrLoginBuilder.setRequired(true);
		ocBuilder.addAttributeInfo(attrNameBuilder.build());
		// role
		AttributeInfoBuilder attrRoleBuilder = new AttributeInfoBuilder(ATTR_ROLE);
		ocBuilder.addAttributeInfo(attrRoleBuilder.build());
		// language
		AttributeInfoBuilder attrLanguageBuilder = new AttributeInfoBuilder(ATTR_LANGUAGE);
		ocBuilder.addAttributeInfo(attrLanguageBuilder.build());
		// is_sync_enabled
		AttributeInfoBuilder attrIsSyncEnabledBuilder = new AttributeInfoBuilder(ATTR_SYNC);
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
		// tracking_code
		AttributeInfoBuilder attrTrackingCodeBuilder = new AttributeInfoBuilder(ATTR_CODE);
		ocBuilder.addAttributeInfo(attrTrackingCodeBuilder.build());
		// can_see_managed_users
		AttributeInfoBuilder attrCanSeeManagedUsersBuilder = new AttributeInfoBuilder(ATTR_MANAGED);
		ocBuilder.addAttributeInfo(attrCanSeeManagedUsersBuilder.build());
		// timezone
		AttributeInfoBuilder attrTimezoneBuilder = new AttributeInfoBuilder(ATTR_TIMEZONE);
		ocBuilder.addAttributeInfo(attrTimezoneBuilder.build());
		// is_exempt_from_device_limits
		AttributeInfoBuilder attrIsExemptFromDeviceLimits = new AttributeInfoBuilder(ATTR_DEVICELIMITS);
		ocBuilder.addAttributeInfo(attrIsExemptFromDeviceLimits.build());
		// is_exempt_from_login_verification
		AttributeInfoBuilder attrIsExemptFromLoginVerification = new AttributeInfoBuilder(ATTR_LOGINVERIFICATION);
		ocBuilder.addAttributeInfo(attrIsExemptFromLoginVerification.build());

		schemaBuilder.defineObjectClass(ocBuilder.build());
		return schemaBuilder.build();
	}

	@Override
	public Uid addAttributeValues(ObjectClass arg0, Uid arg1, Set<Attribute> arg2, OperationOptions arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uid removeAttributeValues(ObjectClass arg0, Uid arg1, Set<Attribute> arg2, OperationOptions arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {
		if (objectClass.is(ObjectClass.ACCOUNT_NAME))
			try {
				return createOrUpdateUser(uid, attributes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;

	}

	@Override
	public FilterTranslator<Filter> createFilterTranslator(ObjectClass arg0, OperationOptions arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
		Name name = null;
		Uid uid = null;
		if (query instanceof StartsWithFilter && ((StartsWithFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();

			if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
				if (name != null) {
					try {
						HttpGet request = new HttpGet(
								getURIBuilder().addParameter(FILTERTERM, name.getNameValue()).build());
						handleUsers(request, handler, options);

					} catch (URISyntaxException | IOException | ParseException e) {

						e.printStackTrace();
					}

				}
			}
		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
			uid = (Uid) ((EqualsFilter) query).getAttribute();

			if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
				if (uid != null) {
					try {
						HttpGet request = new HttpGet(getURIBuilder().setPath(uid.getUidValue().toString()).build());
						JSONObject user = callRequest(request, true);
						ConnectorObject connectorObject = convertUserToConnectorObject(user);
						handler.handle(connectorObject);
					} catch (URISyntaxException | IOException | ParseException e) {

						e.printStackTrace();
					}
				}
			}
		} else {
			throw new NoSuchMethodError("Usuported query filter");

		}

	}

	private boolean handleUsers(HttpGet request, ResultsHandler handler, OperationOptions options)
			throws IOException, ParseException, JSONException, URISyntaxException {
		JSONObject result = callRequest(request);
		JSONArray users = result.getJSONArray("entries");

		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			LOG.ok("response body Handle user: {0}", user.toString());

			ConnectorObject connectorObject = convertUserToConnectorObject(user);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return true;
			}

		}

		return false;
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			LOG.ok("delete user, Uid: {0}", uid);
			HttpDelete request;
			try {
				request = new HttpDelete(getURIBuilder().setPath(uid.getUidValue().toString()).build());
				callRequest(request, false);
			} catch (URISyntaxException | IOException | ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}

	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions operationOptions) {
		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) { // _ACCOUNT_
			try {
				return createOrUpdateUser(null, attributes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

	private Uid createOrUpdateUser(Uid uid, Set<Attribute> attributes)
			throws IOException, ParseException, URISyntaxException {
		LOG.ok("createOrUpdateUser, Uid: {0}, attributes: {1}", uid, attributes);
		if (attributes == null || attributes.isEmpty()) {
			LOG.ok("request ingnored, empty attributes");
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
		String name = getStringAttr(attributes, ATTR_NAME);
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
		putFieldIfExists(attributes, ATTR_CODE, json);
		putFieldIfExists(attributes, ATTR_MANAGED, json);
		putFieldIfExists(attributes, ATTR_TIMEZONE, json);
		putFieldIfExists(attributes, ATTR_DEVICELIMITS, json);
		putFieldIfExists(attributes, ATTR_LOGINVERIFICATION, json);

		HttpEntityEnclosingRequestBase request;
		if (create) {
			request = new HttpPost(getURIBuilder().build());

		} else {
			// update
			request = new HttpPut(getURIBuilder().setPath(uid.getUidValue().toString()).build());
		}
		JSONObject jsonReq = callRequest(request, json);

		String newUid = jsonReq.getString(UID);
		LOG.info("response UID: {0}", uid);

		return new Uid(newUid);
	}

	private void putFieldIfExists(Set<Attribute> attributes, String fieldName, JSONObject jo) {
		String fieldValue = getStringAttr(attributes, fieldName);

		if (fieldValue != null) {
			jo.put(fieldName, fieldValue);
		}
	}

	protected JSONObject callRequest(HttpEntityEnclosingRequestBase request, JSONObject json)
			throws IOException, ParseException {
		LOG.ok("request URI: {0}", request.getURI());
		String accessToken = "Bearer " + refreshToken();
		LOG.info("Token: " + accessToken);
		request.addHeader("Content-Type", CONTENT_TYPE);
		request.addHeader("Authorization", accessToken);
		request.setHeader("Accept-Charset", "utf-8");
		HttpEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"));
		request.setEntity(entity);
		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(request);
		LOG.ok("response: {0}", response);
		// processBoxResponseErrors(response);

		String result = EntityUtils.toString(response.getEntity());
		LOG.ok("response body: {0}", result);

		return new JSONObject(result);
	}

	protected JSONObject callRequest(HttpRequestBase request, boolean parseResult) throws IOException, ParseException {
		LOG.ok("request URI: {0}", request.getURI());
		String accessToken = "Bearer " + refreshToken();
		LOG.info("Token: " + accessToken);
		request.setHeader("Content-Type", CONTENT_TYPE);
		request.addHeader("Authorization", accessToken);
		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(request);

		if (!parseResult) {
			return null;
		}
		String result = EntityUtils.toString(response.getEntity());
		LOG.ok("response body: {0}", result);
		return new JSONObject(result);

	}

	protected JSONObject callRequest(HttpRequestBase request) throws IOException, ParseException {
		LOG.ok("request URI: {0}", request.getURI());
		String accessToken = "Bearer " + refreshToken();
		LOG.info("Token: " + accessToken);
		request.setHeader("Content-Type", CONTENT_TYPE);
		request.addHeader("Authorization", accessToken);
		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(request);

		String result = EntityUtils.toString(response.getEntity());
		LOG.ok("response body: {0}", result);
		return new JSONObject(result);

	}

	/*
	 * private void processBoxResponseErrors(HttpResponse response) throws
	 * IOException { int statusCode = response.getStatusLine().getStatusCode();
	 * if (statusCode == 406) { String result =
	 * EntityUtils.toString(response.getEntity()); if
	 * (result.contains("There is no user with ID")) { throw new
	 * UnknownUidException(result); } JSONObject err; try {
	 * LOG.ok("Result body: {0}", result); JSONObject jo = new
	 * JSONObject(result); err = jo.getJSONObject("form_errors"); } catch
	 * (JSONException e){ throw new
	 * ConnectorIOException(e.getMessage()+" when parsing result: "+result, e);
	 * } if (err.has(ATTR_NAME)) { throw new
	 * AlreadyExistsException(err.getString(ATTR_NAME)); // The name
	 * test_evolveum is already taken. } else if (err.has(ATTR_LOGIN)) { throw
	 * new AlreadyExistsException(err.getString(ATTR_LOGIN)); // The e-mail
	 * address test@evolveum.com is already taken. } else { throw new
	 * ConnectorIOException("Error when creating user: " + result); } }
	 * super.processResponseErrors((CloseableHttpResponse) response); }
	 */

	private void getIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName)) {
			if (object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
				addAttr(builder, attrName, object.get(attrName));
			}

		}

	}

	private ConnectorObject convertUserToConnectorObject(JSONObject user) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(user.getString(UID)));
		if (user.has(ATTR_NAME)) {
			builder.setName(user.getString(ATTR_NAME));
		}

		getIfExists(user, ATTR_LOGIN, builder);
		getIfExists(user, ATTR_ADDRESS, builder);
		getIfExists(user, ATTR_CODE, builder);
		getIfExists(user, ATTR_DEVICELIMITS, builder);
		getIfExists(user, ATTR_LANGUAGE, builder);
		getIfExists(user, ATTR_LOGINVERIFICATION, builder);
		getIfExists(user, ATTR_MANAGED, builder);
		getIfExists(user, ATTR_PHONE, builder);
		getIfExists(user, ATTR_ROLE, builder);
		getIfExists(user, ATTR_SPACE, builder);
		getIfExists(user, ATTR_SYNC, builder);
		getIfExists(user, ATTR_TIMEZONE, builder);
		getIfExists(user, ATTR_TITLE, builder);

		ConnectorObject connectorObject = builder.build();
		LOG.ok("convertUserToConnectorObject, user: {0}, \n\tconnectorObject: {1}", user.getString(UID),
				connectorObject);

		return connectorObject;

	}

	private String refreshToken() throws IOException, org.apache.http.ParseException, ParseException {
		URL location = BoxRestConnector.class.getProtectionDomain().getCodeSource().getLocation();
		LOG.info("Location: " + location.getPath());
		File newFile = new File(location.getPath() + "Token");
		if (!newFile.exists()) {
			newFile.createNewFile();
		}
		FileReader file = new FileReader(location.getPath() + "Token");
		BufferedReader reader = new BufferedReader(file);

		String fileRefreshToken = "";
		String line = reader.readLine();
		while (line != null) {
			fileRefreshToken += line;
			line = reader.readLine();
		}
		reader.close();
		LOG.info("OldToken: {0}", fileRefreshToken);

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("https://api.box.com/oauth2/token");
		post.setHeader("Content-Type", CONTENT_TYPE);
		StringBuilder sb = new StringBuilder();

		if (newFile.exists() && !fileRefreshToken.isEmpty()) {

			sb.append("grant_type").append('=').append(grantType).append('&').append("refresh_token").append('=')
					.append(fileRefreshToken).append('&').append("client_id").append('=').append(clientID).append('&')
					.append("client_secret").append('=').append(clientSecret);
		} else {

			sb.append("grant_type").append('=').append(grantType).append('&').append("refresh_token").append('=')
					.append(refreshToken).append('&').append("client_id").append('=').append(clientID).append('&')
					.append("client_secret").append('=').append(clientSecret);
		}

		post.setEntity(new StringEntity(sb.toString()));
		HttpResponse response = client.execute(post);

		try {
			JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
			refreshToken = (String) json.get("refresh_token");
			accessToken = (String) json.get("access_token");

			try {
				FileWriter fileW = new FileWriter(newFile);
				BufferedWriter buffW = new BufferedWriter(fileW);
				buffW.write((String) json.get("refresh_token"));
				buffW.close();
				System.out.println("File written");
			} catch (Exception e) {
				e.printStackTrace();
			}

			LOG.info("NewToken: {0}", refreshToken);
			LOG.info("accessToken: {0}", accessToken);

		} catch (IOException e) {
			throw new IOException("something is wrong");
		}
		return accessToken;
	}

}
