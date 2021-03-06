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

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;

import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;




/**
 * @author semancik
 *
 */
public class BoxConnectorConfiguration extends AbstractConfiguration implements StatefulConfiguration {

	private String clientId;
	private String URI;
	private GuardedString clientSecret = null;
	private GuardedString refreshToken = null;
	private GuardedString accessToken = null;
	

	public BoxConnectorConfiguration() {
	}
	

	@ConfigurationProperty(order = 1, displayMessageKey = "ClientId", helpMessageKey = "Client identifier issued to the client during the registration process", required = true, confidential = false)

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	
	@ConfigurationProperty(order = 2, displayMessageKey = "ClientSecret", helpMessageKey = "Client secret issued to the client during the registration process", required = true, confidential = false)

	public GuardedString getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(GuardedString clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	
	@ConfigurationProperty(order = 3, displayMessageKey = "RefreshToken", helpMessageKey = "Refresh token allows you to get new access token", required = true, confidential = false)

	public GuardedString getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(GuardedString refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	
	@ConfigurationProperty(order = 4, displayMessageKey = "AccessToken", helpMessageKey = "Access token allows you to execute CRUD operations", required = true, confidential = false)
	
	public GuardedString getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(GuardedString accessToken) {
		this.accessToken = accessToken;
	}
	
	
	@ConfigurationProperty(order = 5, displayMessageKey = "URI", helpMessageKey = "The HTTP endpoint for Box", required = true, confidential = false)

	public String getUri() {
		return URI;
	}

	public void setUri(String URI) {
		this.URI = URI;
	}
	
	
	
	
	
	
	@Override
	public void validate() {
		
		if (StringUtil.isBlank(clientId)) {
            throw new IllegalArgumentException("Client Id cannot be null or empty.");
        }
        if (null == clientSecret) {
            throw new IllegalArgumentException("Client Secret cannot be null or empty.");
        }
        if (null == refreshToken) {
            throw new IllegalArgumentException("Refresh Token cannot be null or empty.");
        }
        if (null == URI) {
            throw new IllegalArgumentException("URI cannot be null or empty.");
        }
        if (null == accessToken) {
        	throw new IllegalArgumentException("Access Token cannot be null or empty.");
        }
	}
	
	@Override
    public void release() {
    }

	

	
	
	

}
