package com.evolveum.polygon.connector.example.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.PreconditionFailedException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.json.JSONObject;

import com.evolveum.polygon.common.GuardedStringAccessor;

public class ObjectsProcessing {

	private static final Log LOG = Log.getLog(BoxConnector.class);
	private static final String ATTR_AVATAR = "avatar_url";
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String grantType = "refresh_token";
	private static final String TOKEN = "/oauth2/token";

	BoxConnectorConfiguration configuration;

	private String accessToken = "";

	public ObjectsProcessing(BoxConnectorConfiguration conf) {
		this.configuration = conf;

	}

	private URIBuilder uri;

	protected URIBuilder getURIBuilder() {
		this.uri = new URIBuilder().setScheme("https").setHost(configuration.getUri());
		return this.uri;
	}

	protected void getAvatarPhoto(JSONObject user, ConnectorObjectBuilder builder, String avatarURL) {

		String avatar = user.getString(avatarURL);
		GuardedString refreshToken = configuration.getRefreshToken();
		GuardedStringAccessor accessorToken = new GuardedStringAccessor();
		refreshToken.access(accessorToken);

		HttpGet request = new HttpGet(avatar);
		request.setHeader("Content-Type", CONTENT_TYPE);
		request.addHeader("Authorization", accessorToken.getClearString());
		request.addHeader("Accept", "image/jpeg");
		request.addHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

		CloseableHttpClient client = HttpClientBuilder.create().build();

		CloseableHttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to execute request:").append(request.toString()).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorIOException(sb.toString(), e);
		}

		processResponseErrors(response);
		HttpEntity responseEntity = response.getEntity();
		byte[] byteJPEG = null;
		try {
			byteJPEG = EntityUtils.toByteArray(responseEntity);
		} catch (IOException e) {
			throw new ConnectorIOException();
		}

		builder.addAttribute(ATTR_AVATAR, byteJPEG);

	}

	protected void putFieldIfExists(Set<Attribute> attributes, String fieldName, JSONObject jo) {
		String fieldValue = getStringAttr(attributes, fieldName);

		if (fieldValue != null) {
			jo.put(fieldName, fieldValue);
		}
	}

	public void putChildFieldIfExists(Set<Attribute> attributes, String fieldName, String attrName, JSONObject jo) {
		String fieldValue = getStringAttr(attributes, attrName);

		if (fieldValue != null) {

			jo.put(fieldName, fieldValue);
		}
	}

	protected JSONObject callRequest(HttpEntityEnclosingRequestBase request, JSONObject json) {

		HttpEntity entity = null;
		try {
			entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Unsupported Encoding when creating object in Box").append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		request.setEntity(entity);
		LOG.info("Create", "OK");
		HttpResponse response = executeRequest(request);
		LOG.info("Create", "OK");
		String result = null;
		try {
			LOG.info("Create", "OK");
			result = EntityUtils.toString(response.getEntity());
			LOG.info("Create", "OK");
		} catch (org.apache.http.ParseException e) {
			throw new ConnectorException();
		} catch (IOException e) {
			throw new ConnectorIOException();
		}
		LOG.info("Create", "OK");
		return new JSONObject(result);
	}

	protected JSONObject callRequest(HttpRequestBase request, boolean parseResult) {

		CloseableHttpResponse response = executeRequest(request);
		if (!parseResult) {
			return null;
		}
		String result = null;
		try {
			result = EntityUtils.toString(response.getEntity());
		} catch (org.apache.http.ParseException e) {
			throw new ConnectorException();
		} catch (IOException e) {
			throw new ConnectorIOException();
		}
		processResponseErrors(response);
		return new JSONObject(result);

	}

	protected JSONObject callRequest(HttpRequestBase request) {

		CloseableHttpResponse response = executeRequest(request);
		String result = null;
		try {
			result = EntityUtils.toString(response.getEntity());
		} catch (org.apache.http.ParseException e) {
			throw new ConnectorException();
		} catch (IOException e) {

			throw new ConnectorIOException();
		}
		processResponseErrors(response);
		return new JSONObject(result);

	}

	protected void getIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName)) {
			if (object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
				addAttr(builder, attrName, object.get(attrName));
			}

		}

	}

	private String refreshToken() {
		URI uri = null;
		GuardedString refreshToken = configuration.getRefreshToken();
		GuardedStringAccessor accessorToken = new GuardedStringAccessor();
		refreshToken.access(accessorToken);

		GuardedString clientSecret = configuration.getClientSecret();
		GuardedStringAccessor accessorSecret = new GuardedStringAccessor();
		clientSecret.access(accessorSecret);

		CloseableHttpClient client = HttpClientBuilder.create().build();

		try {
			uri = getURIBuilder().setPath(TOKEN).build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		HttpPost post = new HttpPost(uri);
		post.setHeader("Content-Type", CONTENT_TYPE);
		StringBuilder sb = new StringBuilder();

		sb.append("grant_type").append('=').append(grantType).append('&').append("refresh_token").append('=')
				.append(accessorToken.getClearString()).append('&').append("client_id").append('=')
				.append(configuration.getClientId()).append('&').append("client_secret").append('=')
				.append(accessorSecret.getClearString());

		try {
			post.setEntity(new StringEntity(sb.toString()));
		} catch (UnsupportedEncodingException e) {
			StringBuilder sbuilder = new StringBuilder();
			sbuilder.append("Unsupported Encoding when creating object in Box").append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		CloseableHttpResponse response = null;

		try {
			response = (CloseableHttpResponse) client.execute(post);

		} catch (IOException e) {
			StringBuilder sbPost = new StringBuilder();
			sb.append("It was not possible execute HttpUriRequest:").append(post).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorIOException(sbPost.toString(), e);
		}

		processResponseErrors(response);

		JSONObject json = null;
		try {
			json = new JSONObject(EntityUtils.toString(response.getEntity()));

		} catch (IOException e) {
			StringBuilder sbJSON = new StringBuilder();
			sb.append("Not possible to get response").append(response).append(";").append(e.getLocalizedMessage());
			throw new ConnectorIOException(sbJSON.toString(), e);
		}
		String accessTokenJson = (String) json.get("access_token");
		String token = (String) json.get("refresh_token");

		GuardedString refreshTokenSet = new GuardedString(new String(token).toCharArray());
		configuration.setRefreshToken(refreshTokenSet);
		LOG.info("NewToken: {0}", token);
		
		GuardedString accessTokenSet = new GuardedString(new String(accessTokenJson).toCharArray());
		configuration.setAccessToken(accessTokenSet);
		
		accessToken = "Bearer "+accessTokenJson;
		LOG.info("accessToken: {0}", accessToken);

		return accessToken;

	}

	protected String getStringAttr(Set<Attribute> attributes, String attrName) throws InvalidAttributeValueException {
		return getAttr(attributes, attrName, String.class);
	}

	private <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type)
			throws InvalidAttributeValueException {
		return getAttr(attributes, attrName, type, null);
	}

	private <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal)
			throws InvalidAttributeValueException {
		for (Attribute attr : attributes) {
			if (attrName.equals(attr.getName())) {
				List<Object> vals = attr.getValue();
				if (vals == null || vals.isEmpty()) {
					return defaultVal;
				}
				if (vals.size() == 1) {
					Object val = vals.get(0);
					if (val == null) {
						return defaultVal;
					}
					if (type.isAssignableFrom(val.getClass())) {
						return (T) val;
					}
					throw new InvalidAttributeValueException(
							"Unsupported type " + val.getClass() + " for attribute " + attrName);
				}
				throw new InvalidAttributeValueException("More than one value for attribute " + attrName);
			}
		}
		return defaultVal;
	}

	private <T> void addAttr(ConnectorObjectBuilder builder, String attrName, T attrVal) {
		if (attrVal != null) {
			builder.addAttribute(attrName, attrVal);
		}
	}

	public void processResponseErrors(CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode >= 200 && statusCode <= 299) {
			return;
		}
		String responseBody = null;
		try {
			responseBody = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			LOG.warn("cannot read response body: " + e, e);
		}

		String message = "HTTP error " + statusCode + " " + response.getStatusLine().getReasonPhrase() + " : "
				+ responseBody;
		LOG.error("{0}", message);
		if (statusCode == 400 || statusCode == 405 || statusCode == 406) {
			closeResponse(response);
			throw new ConnectorIOException(message);
		}
		if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 407) {
			closeResponse(response);
			throw new PermissionDeniedException(message);
		}
		if (statusCode == 404 || statusCode == 410) {
			closeResponse(response);
			throw new UnknownUidException(message);
		}
		if (statusCode == 408) {
			closeResponse(response);
			throw new OperationTimeoutException(message);
		}
		if (statusCode == 412) {
			closeResponse(response);
			throw new PreconditionFailedException(message);
		}
		if (statusCode == 418) {
			closeResponse(response);
			throw new UnsupportedOperationException("Sorry, no cofee: " + message);
		}

		closeResponse(response);
		throw new ConnectorException(message);
	}

	protected void closeResponse(CloseableHttpResponse response) {
		// to avoid pool waiting
		try {
			response.close();
		} catch (IOException e) {
			LOG.warn(e, "Error when trying to close response: " + response);
		}
	}

	public CloseableHttpResponse executeRequest(HttpUriRequest request) {
		GuardedString accessTokenConf = configuration.getAccessToken();
		GuardedStringAccessor accessorToken = new GuardedStringAccessor();
		accessTokenConf.access(accessorToken);
		if (accessorToken.getClearString().isEmpty()) {
			refreshToken();
			
		}
			
			String accessToken = "Bearer " + accessorToken.getClearString();
			request.setHeader("Content-Type", CONTENT_TYPE);
			request.addHeader("Authorization", accessToken);
			CloseableHttpClient client = HttpClientBuilder.create().build();
			CloseableHttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (IOException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to execute request:").append(request.toString()).append(";")
						.append(e.getLocalizedMessage());
				throw new ConnectorIOException(sb.toString(), e);
			}
			if (response.getStatusLine().getStatusCode() == 401) {
				return executeRequestWithRefresh(request);
			}
			return response;
		
	}
	
	public CloseableHttpResponse executeRequestWithRefresh(HttpUriRequest request) {
		request.setHeader("Content-Type", CONTENT_TYPE);
		request.removeHeaders("Authorization");
		request.addHeader("Authorization", refreshToken());
		CloseableHttpClient client = HttpClientBuilder.create().build();
		CloseableHttpResponse response = null;
		try {
			response = client.execute(request);
			
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to execute request:").append(request.toString()).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorIOException(sb.toString(), e);
		}
		return response;
	}

}
