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

/**
 * This class will hold constants related to Nuxeo authenticator implementation.
 */
public class NuxeoAuthenticatorConstants {
    public static final String AUTHENTICATOR_NAME = "nuxeoAuthenticator";
    public static final String AUTHENTICATOR_FRIENDLY_NAME = "nuxeo";
    public static final String CLIENT_ID = "Client Id";
    public static final String CLIENT_SECRET = "Client Secret";
    public static final String CALLBACK_URL = "Callback URL";
    public static final String LOGGED_IN_USER_IDENTIFIER = "id";
    public static final String AUTHORIZATION = "Authorization";
    public static final String AUTHENTICATION_BEARER = "Bearer ";
    //Nuxeo authorize endpoint URL
    public static final String NUXEO_OAUTH_ENDPOINT = "/nuxeo/oauth2/authorize";
    //Nuxeo token  endpoint URL
    public static final String NUXEO_TOKEN_ENDPOINT = "/nuxeo/oauth2/token";
    //Nuxeo user info endpoint URL
    public static final String NUXEO_USERINFO_ENDPOINT = "/nuxeo/api/v1/me";
    //Nuxeo sever URL prefix
    public static final String NUXEO_SERVER_URL = "Nuxeo Server URL";
    public static final String CLAIM_DIALECT_URI = "http://wso2.org/nuxeo/claims";

    NuxeoAuthenticatorConstants() {
    }
}
