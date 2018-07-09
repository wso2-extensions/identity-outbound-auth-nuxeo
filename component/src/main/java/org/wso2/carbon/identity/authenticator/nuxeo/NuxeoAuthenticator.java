/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.authenticator.nuxeo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants;
import org.wso2.carbon.identity.application.authenticator.oidc.OpenIDConnectAuthenticator;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The class is implementing the Authenticator for Nuxeo.
 */
public class NuxeoAuthenticator extends OpenIDConnectAuthenticator implements FederatedApplicationAuthenticator {

    private static Log log = LogFactory.getLog(NuxeoAuthenticator.class);

    /**
     * Get Nuxeo authorization endpoint.
     *
     * @param authenticatorProperties Authenticator properties.
     * @return the authorization endpoint.
     */
    @Override
    protected String getAuthorizationServerEndpoint(Map<String, String> authenticatorProperties) {

        return authenticatorProperties.get(NuxeoAuthenticatorConstants.NUXEO_SERVER_URL)
                + NuxeoAuthenticatorConstants.NUXEO_OAUTH_ENDPOINT;
    }

    /**
     * Get the Nuxeo token endpoint.
     *
     * @param authenticatorProperties Authenticator properties.
     * @return the token endpoint.
     */
    @Override
    protected String getTokenEndpoint(Map<String, String> authenticatorProperties) {

        return authenticatorProperties.get(NuxeoAuthenticatorConstants.NUXEO_SERVER_URL)
                + NuxeoAuthenticatorConstants.NUXEO_TOKEN_ENDPOINT;
    }

    /**
     * Get the friendly name of the Authenticator.
     *
     * @return the friendly name of the Authenticator.
     */
    @Override
    public String getFriendlyName() {

        return NuxeoAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }

    /**
     * Get the name of the Authenticator.
     */
    @Override
    public String getName() {

        return NuxeoAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    /**
     * Get the Nuxeo specific claim dialect URI.
     *
     * @return Claim dialect URI.
     */
    @Override
    public String getClaimDialectURI() {
        return NuxeoAuthenticatorConstants.CLAIM_DIALECT_URI;
    }

    /**
     * Get Nuxeo user info endpoint.
     *
     * @param authenticatorProperties Authenticator properties.
     * @return the user info endpoint.
     */
    private String getUserInfoEndpoint(Map<String, String> authenticatorProperties) {

        return authenticatorProperties.get(NuxeoAuthenticatorConstants.NUXEO_SERVER_URL)
                + NuxeoAuthenticatorConstants.NUXEO_USERINFO_ENDPOINT;
    }

    /**
     * Get Configuration Properties.
     */
    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        Property clientId = new Property();
        clientId.setName(OIDCAuthenticatorConstants.CLIENT_ID);
        clientId.setDisplayName(NuxeoAuthenticatorConstants.CLIENT_ID);
        clientId.setRequired(true);
        clientId.setDescription("Enter Nuxeo client identifier value");
        clientId.setDisplayOrder(0);
        configProperties.add(clientId);

        Property clientSecret = new Property();
        clientSecret.setName(OIDCAuthenticatorConstants.CLIENT_SECRET);
        clientSecret.setDisplayName(NuxeoAuthenticatorConstants.CLIENT_SECRET);
        clientSecret.setRequired(true);
        clientSecret.setConfidential(true);
        clientSecret.setDescription("Enter Nuxeo client secret value");
        clientSecret.setDisplayOrder(1);
        configProperties.add(clientSecret);

        Property callbackUrl = new Property();
        callbackUrl.setDisplayName(NuxeoAuthenticatorConstants.CALLBACK_URL);
        callbackUrl.setName(IdentityApplicationConstants.OAuth2.CALLBACK_URL);
        callbackUrl.setDescription("Enter the callback url");
        callbackUrl.setDisplayOrder(2);
        configProperties.add(callbackUrl);

        Property nuxeoServerUrl = new Property();
        nuxeoServerUrl.setDisplayName(NuxeoAuthenticatorConstants.NUXEO_SERVER_URL);
        nuxeoServerUrl.setName(NuxeoAuthenticatorConstants.NUXEO_SERVER_URL);
        nuxeoServerUrl.setDescription("Enter the Nuxeo server url. Eg: http://localhost:8080");
        nuxeoServerUrl.setDisplayOrder(3);
        configProperties.add(nuxeoServerUrl);

        return configProperties;
    }

    /**
     * This method is overridden to check additional condition like whether request is having
     * sendToken, token parameters, generateNuxeoToken and authentication name.
     *
     * @param request  Http servlet request
     * @param response Http servlet response
     * @param context  AuthenticationContext
     * @throws AuthenticationFailedException User tenant domain mismatch
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

        String clientId = null;
        try {
            Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();
            clientId = authenticatorProperties.get(OIDCAuthenticatorConstants.CLIENT_ID);
            String clientSecret = authenticatorProperties.get(OIDCAuthenticatorConstants.CLIENT_SECRET);
            String tokenEndPoint = getTokenEndpoint(authenticatorProperties);
            String callbackUrl = getCallbackUrl(authenticatorProperties);
            String userInfoUrl = getUserInfoEndpoint(authenticatorProperties);
            OAuthAuthzResponse authorizationResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
            String code = authorizationResponse.getCode();
            AuthenticatedUser authenticatedUserObj;
            Map<ClaimMapping, String> claims;
            OAuthClientRequest accessRequest = getAccessRequest(tokenEndPoint, clientId, code, clientSecret,
                    callbackUrl);
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthClientResponse oAuthResponse = getResponseOauth(oAuthClient, accessRequest);
            String accessToken = oAuthResponse.getParam(OIDCAuthenticatorConstants.ACCESS_TOKEN);
            String loggedInUserInfo = getUserInfoJson(userInfoUrl, accessToken);
            if (loggedInUserInfo.isEmpty()) {
                throw new AuthenticationFailedException("Getting empty or null value for logged in user info from " +
                        "a user info endpoint: " + userInfoUrl);
            }
            Object loggedInUserID = JSONUtils.parseJSON(loggedInUserInfo).get(NuxeoAuthenticatorConstants.
                    LOGGED_IN_USER_IDENTIFIER);
            claims = mapUserInfo(loggedInUserInfo);
            authenticatedUserObj = AuthenticatedUser.createFederateAuthenticatedUserFromSubjectIdentifier(
                    String.valueOf(loggedInUserID));
            authenticatedUserObj.setAuthenticatedSubjectIdentifier(String.valueOf(loggedInUserID));
            authenticatedUserObj.setUserAttributes(claims);
            context.setSubject(authenticatedUserObj);
        } catch (OAuthProblemException e) {
            throw new AuthenticationFailedException("Exception while getting the oAuth code from HttpServletRequest "
                    + "for the client: " + clientId + ". Check whether you used the client_Secret value that " +
                    "associate with clien_ID: " + clientId, e);
        }
    }

    /**
     * Get the OAuth response for access token.
     *
     * @param oAuthClient   the OAuthClient.
     * @param accessRequest the AccessRequest.
     * @return Response for access token from service provider.
     */
    private OAuthClientResponse getResponseOauth(OAuthClient oAuthClient, OAuthClientRequest accessRequest)
            throws AuthenticationFailedException {

        OAuthClientResponse oAuthResponse;
        try {
            oAuthResponse = oAuthClient.accessToken(accessRequest);
        } catch (OAuthProblemException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception occurred from the invalid client_ID or Client_Secret: "
                        + accessRequest.getBody() + " while getting the OAuth response for access token.");
            }
            throw new AuthenticationFailedException("Exception occurred from the invalid client_ID or Client_Secret " +
                    "which are used to get the OAuth response from the URL : " + accessRequest.getLocationUri() +
                    " Check the client_Secret value that associate with client_ID. ", e);
        } catch (OAuthSystemException e) {
            throw new AuthenticationFailedException("Communication errors between the OAuth Recourse Server and " +
                    "the OAuth Authorization Server while getting the OAuth response form the URL: " +
                    accessRequest.getLocationUri(), e);
        }
        return oAuthResponse;
    }

    /**
     * Get the access token from endpoint with generated code.
     *
     * @param tokenEndPoint the Access_token endpoint.
     * @param clientId      the Client ID.
     * @param code          the Code.
     * @param clientSecret  the Client Secret.
     * @param callbackurl   the CallBack URL.
     * @return the access token
     */
    private OAuthClientRequest getAccessRequest(String tokenEndPoint, String clientId, String code, String clientSecret,
                                                String callbackurl) throws AuthenticationFailedException {

        OAuthClientRequest accessRequest;
        try {
            accessRequest = OAuthClientRequest.tokenLocation(tokenEndPoint).setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(clientId).setClientSecret(clientSecret).setRedirectURI(callbackurl).setCode(code)
                    .buildBodyMessage();
        } catch (OAuthSystemException e) {
            throw new AuthenticationFailedException("Error occurred while building the token request with " +
                    "the parameters are client_Id: " + clientId + " ,client_Secret, token_End_Point: " + tokenEndPoint +
                    " and callback URL: " + callbackurl, e);
        }
        return accessRequest;
    }

    /**
     * Get the user info from user info endpoint.
     *
     * @param url         User info endpoint.
     * @param accessToken Access token.
     * @return Response string.
     */
    private String getUserInfoJson(String url, String accessToken) throws AuthenticationFailedException {

        if (log.isDebugEnabled()) {
            log.debug("User info endpoint: " + url);
        }
        BufferedReader reader = null;
        StringBuilder result = null;
        Integer statusCode;
        String line;
        HttpGet httpget = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            httpget = new HttpGet(url);
            httpget.setHeader(NuxeoAuthenticatorConstants.AUTHORIZATION,
                    NuxeoAuthenticatorConstants.AUTHENTICATION_BEARER + accessToken);
            result = new StringBuilder();
            statusCode = httpclient.execute(httpget).getStatusLine().getStatusCode();
            reader = new BufferedReader(new InputStreamReader(httpclient.execute(httpget).getEntity().getContent()));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            if (statusCode == HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("logged in User info: " + result.toString());
                }
                return result.toString();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to get a successful response while getting UserInfo for the logged in User. " +
                            "Status code: " + statusCode + " and Response: " + result.toString());
                }
                throw new AuthenticationFailedException(" Unable to get a successful response while getting " +
                        "UserInfo for the logged in User. Status code: " + statusCode + " and Response: " +
                        result.toString());
            }
        } catch (ClientProtocolException e) {
            throw new AuthenticationFailedException("Exception occurred while invoking the URL:" + httpget.getURI(), e);
        } catch (IOException e) {
            throw new AuthenticationFailedException(" I/O error occurred while invoking the user info endpoint: " +
                    httpget.getURI(), e);
        }
    }

    /**
     * Get the user claims from user info.
     *
     * @param userInfo User info.
     * @return Response Map<ClaimMapping, String>.
     */
    protected Map<ClaimMapping, String> mapUserInfo(String userInfo) {

        Map<ClaimMapping, String> claims = new HashMap<>();
        JSONObject userInfoInJsonObj = new JSONObject(userInfo);
        Iterator parentKey = userInfoInJsonObj.keys();
        String claimsDialectUri = getClaimDialectURI();
        while (parentKey.hasNext()) {
            String eachParentkey = (String) parentKey.next();
            Object parentData = userInfoInJsonObj.get(eachParentkey);
            if (parentData instanceof JSONObject && eachParentkey.equals("properties")) {
                Iterator childKey = ((JSONObject) parentData).keys();
                while (childKey.hasNext()) {
                    String eachChildKey = (String) childKey.next();
                    Object childData = ((JSONObject) parentData).get(eachChildKey);
                    if (childData instanceof JSONArray && eachChildKey.equals("groups")) {
                        StringJoiner data = new StringJoiner(",");
                        for (int i = 0; i < ((JSONArray) childData).length(); i++) {
                            data.add(((JSONArray) childData).get(i).toString());
                        }
                        String result = data.toString();
                        claims.put(ClaimMapping.build(claimsDialectUri + eachChildKey,
                                claimsDialectUri + eachChildKey, null, false), result);
                        if (log.isDebugEnabled()) {
                            log.debug("Adding claims from the end-point data mapping: " +
                                    claimsDialectUri + eachChildKey + " - " + result);
                        }
                    } else {
                        claims.put(ClaimMapping.build(claimsDialectUri + eachChildKey,
                                claimsDialectUri + eachChildKey, null, false),
                                childData.toString());
                        if (log.isDebugEnabled()) {
                            log.debug("Adding claims from the end-point data mapping: " + claimsDialectUri +
                                    eachParentkey + " - " + childData.toString());
                        }
                    }
                }
            } else {
                claims.put(ClaimMapping.build(claimsDialectUri + eachParentkey,
                        claimsDialectUri + eachParentkey, null, false),
                        parentData.toString());
                if (log.isDebugEnabled()) {
                    log.debug("Adding claims from the end-point data mapping: " +
                            claimsDialectUri + eachParentkey + " - " + parentData.toString());
                }
            }
        }
        return claims;
    }
}
