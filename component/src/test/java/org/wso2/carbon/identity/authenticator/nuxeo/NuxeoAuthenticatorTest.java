/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.authenticator.nuxeo;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class) @PrepareForTest({ OAuthClientRequest.class, OAuthAuthzResponse.class,
        NuxeoAuthenticator.class, HttpClients.class, CloseableHttpClient.class })
public class NuxeoAuthenticatorTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Spy
    private AuthenticationContext context;

    @Mock
    private NuxeoAuthenticator mockNuxeoAuthenticator;

    @Spy
    private NuxeoAuthenticator spy;

    @Mock
    private OAuthAuthzResponse mockOAuthAuthzResponse;

    @Mock
    private OAuthClient mockOAuthClient;

    @Mock
    private OAuthClientRequest mockOAuthClientRequest;

    @Mock
    private OAuthClientResponse oAuthClientResponse;

    private NuxeoAuthenticator nuxeoAuthenticator;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setUp() {
        nuxeoAuthenticator = new NuxeoAuthenticator();
        initMocks(this);
    }

    @DataProvider(name = "authenticatorProperties")
    public Object[][] getAuthenticatorPropertiesData() {
        Map<String, String> authenticatorProperties = new HashMap<>();
        authenticatorProperties.put(OIDCAuthenticatorConstants.CLIENT_ID, "test-client-id");
        authenticatorProperties.put(OIDCAuthenticatorConstants.CLIENT_SECRET, "test-client-secret");
        authenticatorProperties.put(NuxeoAuthenticatorConstants.NUXEO_SERVER_URL, "http://localhost:8080/nuxeo");
        authenticatorProperties.put("callbackUrl", "http://localhost:9443/commonauth");
        return new Object[][] {{authenticatorProperties}};
    }

    @Test(description = "Test case for getAuthorizationServerEndpoint method", dataProvider = "authenticatorProperties")
    public void testGetAuthorizationServerEndpoint(Map<String, String> authenticatorProperties) throws Exception {
        String authorizationEp = Whitebox
                .invokeMethod(nuxeoAuthenticator, "getAuthorizationServerEndpoint", authenticatorProperties);
        Assert.assertEquals("http://localhost:8080/nuxeo/nuxeo/oauth2/authorize", authorizationEp);
    }

    @Test(description = "Test case for getTokenEndpoint method", dataProvider = "authenticatorProperties")
    public void testGetTokenEndpoint(Map<String, String> authenticatorProperties) throws Exception {
        String authorizationEp = Whitebox
                .invokeMethod(nuxeoAuthenticator, "getTokenEndpoint", authenticatorProperties);
        Assert.assertEquals("http://localhost:8080/nuxeo/nuxeo/oauth2/token", authorizationEp);
    }

    @Test(description = "Test case for getFriendlyName method")
    public void testGetFriendlyName() {
        String friendlyName = nuxeoAuthenticator.getFriendlyName();
        Assert.assertEquals(NuxeoAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME, friendlyName);
    }

    @Test(description = "Test case for getName method")
    public void testGetName() {
        String name = nuxeoAuthenticator.getName();
        Assert.assertEquals(NuxeoAuthenticatorConstants.AUTHENTICATOR_NAME, name);
    }

    @Test(description = "Test case for getClaimDialectURI method")
    public void testGetClaimDialectURI() {
        String claimDialectURI = nuxeoAuthenticator.getClaimDialectURI();
        Assert.assertEquals(NuxeoAuthenticatorConstants.CLAIM_DIALECT_URI, claimDialectURI);
    }

    @Test(description = "Test case for getTokenEndpoint method", dataProvider = "authenticatorProperties")
    public void testGetUserInfoEndpoint(Map<String, String> authenticatorProperties) throws Exception {
        String authorizationEp = Whitebox
                .invokeMethod(nuxeoAuthenticator, "getUserInfoEndpoint", authenticatorProperties);
        Assert.assertEquals("http://localhost:8080/nuxeo/nuxeo/api/v1/me", authorizationEp);
    }

    @Test(description = "Test case for getResponseOauth method", dataProvider = "authenticatorProperties")
    public void testProcessAuthenticationResponse(Map<String, String> authenticatorProperties) throws Exception {
        NuxeoAuthenticator spyAuthenticator = PowerMockito.spy(new NuxeoAuthenticator());
        context.setAuthenticatorProperties(authenticatorProperties);
        PowerMockito.mockStatic(OAuthAuthzResponse.class);
        Mockito.when(OAuthAuthzResponse.oauthCodeAuthzResponse(Mockito.any(HttpServletRequest.class)))
                .thenReturn(mockOAuthAuthzResponse);
        PowerMockito.doReturn(oAuthClientResponse)
                .when(spyAuthenticator, "getResponseOauth", Mockito.any(OAuthClient.class),
                        Mockito.any(OAuthClientRequest.class), Mockito.any(AuthenticationContext.class));
        PowerMockito.doReturn("{\"id\":\"testuser\"}")
                .when(spyAuthenticator, "getUserInfoJson", Mockito.anyString(), Mockito.anyString());
        Whitebox.invokeMethod(spyAuthenticator, "processAuthenticationResponse", httpServletRequest,
                httpServletResponse, context);
        Assert.assertNotNull(context.getSubject());
        Assert.assertEquals("testuser", context.getSubject().getAuthenticatedSubjectIdentifier());
    }

    @Test(description = "Test case for getAccessRequest method.")
    public void testGetAccessRequest() throws Exception {
        PowerMockito.mockStatic(OAuthClientRequest.class);
        Mockito.when(OAuthClientRequest.tokenLocation(Mockito.anyString()))
                .thenReturn(new OAuthClientRequest.TokenRequestBuilder("/token"));
        OAuthClientRequest getAccessRequest = Whitebox
                .invokeMethod(nuxeoAuthenticator, "getAccessRequest", "/token", "dummy-clientId", "dummy-code",
                        "dummy-secret", "/callback");
        Assert.assertNotNull(getAccessRequest);
        Assert.assertEquals(getAccessRequest.getLocationUri(), "/token");
    }

    @Test(description = "Test case for getResponseOauth method", dataProvider = "authenticatorProperties")
    public void testGetResponseOauth(Map<String, String> authenticatorProperties) throws Exception {
        context.setAuthenticatorProperties(authenticatorProperties);
        OAuthClientResponse oAuthClientResponse = Whitebox
                .invokeMethod(nuxeoAuthenticator, "getResponseOauth", mockOAuthClient, mockOAuthClientRequest, context);
        Assert.assertNull(oAuthClientResponse);
    }

    @Test(description = "Test case for getUserInfo method")
    public void testGetUserInfo() throws Exception {
        PowerMockito.mockStatic(HttpClients.class);
        CloseableHttpResponse closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient httpClient = PowerMockito.mock(CloseableHttpClient.class);
        BasicStatusLine basicStatusLine = Mockito.mock(BasicStatusLine.class);
        Mockito.when(basicStatusLine.getStatusCode()).thenReturn(200);
        BasicHttpEntity basicHttpEntity = Mockito.mock(BasicHttpEntity.class);
        Mockito.when(basicHttpEntity.getContent()).thenReturn(new ByteArrayInputStream("[{\"sub\":\"admin\"}]".getBytes(
                StandardCharsets.UTF_8)));
        Mockito.when(HttpClients.createDefault()).thenReturn(httpClient);
        Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(closeableHttpResponse);
        Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(basicStatusLine);
        Mockito.when(closeableHttpResponse.getEntity()).thenReturn(basicHttpEntity);
        String userInfoStr = Whitebox.invokeMethod(nuxeoAuthenticator, "getUserInfoJson", "testurl", "dummytoken");
        Assert.assertNotNull(userInfoStr);
        JSONArray userJsonObj = new JSONArray(userInfoStr);
        Assert.assertEquals("admin", ((JSONObject)userJsonObj.get(0)).get("sub"));
    }

    @Test(description = "Test case for mapUserInfo method")
    public void testMapUserInfo() throws Exception {
        String userInfo = "{\"sub\":\"admin\"}";
        Map<ClaimMapping, String> claimMap = Whitebox.invokeMethod(nuxeoAuthenticator, "mapUserInfo", userInfo);
        Assert.assertEquals(1, claimMap.size());
        ClaimMapping claimMapping = claimMap.keySet().iterator().next();
        Assert.assertNotNull(claimMapping);
        Assert.assertEquals("http://wso2.org/nuxeo/claims/sub", claimMapping.getLocalClaim().getClaimUri());
        String claimVal = claimMap.get(claimMapping);
        Assert.assertEquals("admin", claimVal);
    }
}
