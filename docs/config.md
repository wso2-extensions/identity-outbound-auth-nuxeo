# Configuring Nuxeo Authenticator

The topics in this page provide instructions on how to configure the Nuxeo authenticator with WSO2 Identity Server.Here, a sample app  is used to demonstrate the integration. 
 
 ````
  * Nuxeo Authenticator is supported with Identity Server 5.5.0. 
  * Configuring the Nuxeo authenticator is tested with Nuxeo Server version 10.1.
 ````
 
Follow the the instructions in the topics below to configure the Nuxeo authenticator with WSO2 Identity Server:
 
* [Deploying Nuxeo artifacts](#deploying-nuxeo-artifacts)
* [Configuring the Nuxeo App](#configuring-the-nuxeo-app)
* [Deploying travelocity.com sample app](#deploying-travelocitycom-sample-app)
* [Configuring the identity provider](#configuring-the-identity-provider)
* [Configuring the service provider](#configuring-the-service-provider)
* [Configuring claims](#configuring-claims)
* [Configuring requested claims for travelocity.com](#configuring-requested-claims-for-travelocitycom)
* [Testing the sample](#testing-the-sample)

### Deploying Nuxeo artifacts

* Download the artifacts for this authenticator from the [store](https://store.wso2.com/store/assets/isconnector/details/c7003ffb-18a1-48ed-9a99-6274796fa978).
* Copy the downloaded org.wso2.carbon.identity.authenticator.nuxeo-x.x.x.jar file to the <IS_HOME>/repository/components/dropins directory.

> If you want to upgrade the Nuxeo Authenticator (.jar) that is packaged with your WSO2 IS distribution to the latest, see [upgrade instructions](https://docs.wso2.com/display/ISCONNECTORS/Upgrading+an+Authenticator).
 
 ### Configuring the Nuxeo application
1. Go to [https://www.nuxeo.com/downloads/](https://www.nuxeo.com/downloads/), download the server and unzip the archive.The path to the sever will be referred to as <NUEXO_HOME> throughout this page.
2. Navigate to the <NUEXO_HOME>/bin directory and use the following command to install the JSF UI addon on your server.
      ````  
      ./nuxeoctl mp-install nuxeo-jsf-ui
      ````
   For more information please refer [https://doc.nuxeo.com/nxdoc/installing-a-new-package-on-your-instance/](https://doc.nuxeo.com/nxdoc/installing-a-new-package-on-your-instance/)
3. Start the Nuxeo server using the commands given below:
     ````   
      $ chmod +x ./bin/nuxeoctl
      $ ./bin/nuxeoctl start
      ````
4. Once the server starts, follow the steps below to setup the nuxeo server.
5. Go to the [http://localhost:8080/nuxeo/jsf](http://localhost:8080/nuxeo/jsf) 
    1. and sign in with **Administrator/Administrator** credentials.
    2. Click **Admin**, then click **Cloud Services**,and then click the **Consumers** tab.
    3. Click **Add** under the **OAuth2 Clients** section.
    4. Specify values for the **Name**, **Client ID**, **Client Secret**, and **Redirect URI***. You can use **https://localhost:9443/commonauth** as the **Redirect URI**
    5. Click **Create**. 

       ![alt text](images/im1.png) 

Now you have configured the Nuxeo application. 

Next let's deploy the travelocity.com sample app so that it can be used in this scenario.

### Deploying the travelocity.com sample app
    
To download and deploy the travelocity.com sample application,follow the instructions in [deploying travelocity.com sample app](https://docs.wso2.com/display/ISCONNECTORS/Deploying+the+Sample+App).

````
If you are running the nuxeo server and apache tomcat on the same port (eg: 8080), be sure to change the port that you run apache tomcat..
Follow the steps below to change the port on which apache tomcat runs:
    1. Navigate to  <TOMCAT_HOME>/conf/server.xml
                <Connector port="8080" protocol="HTTP/1.1"
                    connectionTimeout="20000"
                   redirectPort="8443" />
    2. Navigate to the <TOMCAT_HOME>/webapps/travelocity.com/WEB-INF/classes/travelocity.properties file and change the port in the URL of the SAML 2.0 assertion consumer.
                #The URL of the SAML 2.0 Assertion Consumer
                SAML2.AssertionConsumerURL=http://localhost:8080/travelocity.com/home.jsp
            
````

### Configuring the identity provider

Follow the steps below to add [a new identity provider](https://docs.wso2.com/display/IS510/Configuring+an+Identity+Provider) via the management console of WSO2 Identity Server..

1. Download the WSO2 Identity Server from [here](https://wso2.com/identity-and-access-management) and [run it](https://docs.wso2.com/display/IS510/Running+the+Product).

2. Log in to the [management console](https://docs.wso2.com/display/IS510/Getting+Started+with+the+Management+Console) as an administrator.

3. In the **Identity Providers** section under the **Main** tab of the management console, click **Add**.

4. Give a suitable name for **Identity Provider Name**.
    ![alt text](images/im2.png)
5. Navigate to **Nuxeo Configuration** under **Federated Authenticators**.

6. Enter the values as given in the above figure.
    
    | Field| Description | Sample Values |
    | ------------- |-------------| ---------------|
    | Enable    | Selecting this option enables Dropbox to be used as an authenticator for users provisioned to WSO2 Identity Server. | Selected |
    | Default    | Selecting the **Default** checkbox specifies **Nuxeo** as the main/default form of authentication. If selected, any other authenticators that have been selected as Default will be unselected by WSO2 IS. | Selected |
    | Client ID | This is the Client Id from the Nuxeo App. | clientApp |
    | Client Secret | This is the Client Secret from the Nuxeo App. Click the Show button to view the value you enter. |clientsecret|
    | Callback URL | The URL to which the browser should be redirected to after the authentication is successful. Follow this format: https://(host-name):(port)/acs . |[https://localhost:9443/commonauth](https://localhost:9443/commonauth) |
    |Nuxeo server URL|The Nuxeo server URL|http://localhost:8080|

7. Click **Register**.

Now you have added the identity provider.

### Configuring the service provider

The next step is to configure the service provider.

1. Return to the management console.

2. In the **Service Providers** section under the **Main** tab, click **Add**.

3. Since you are using travelocity as the sample, enter travelocity.com in the **Service Provider Name** text box and 
click **Register**.
 
4. In the **Inbound Authentication Configuration** section, click **Configure** under the **SAML2 Web SSO 
Configuration** 
section.

    ![alt text](images/im3.png)

5. Now set the configuration as follows:
    
    1. **Issuer**: travelocity.com
    2. **Assertion Consumer URL**: [http://localhost:8080/travelocity.com/home.jsp](http://localhost:8080/travelocity.com/home.jsp)
6. Select the following check-boxes:
    1. **Enable Response Signing**.
    2. **Enable Single Logout**.
    3. **Enable Attribute Profile**.
    4. **Include Attributes in the Response Always**.

7. Click **Update** to save the changes. Now you will be sent back to the **Service Providers** page.

8. Go to the **Local and Outbound Authentication Configuration** section.

9. Select the identity provider you created from the dropdown list under **Federated Authentication**.

    ![alt text](images/im4.png)

10. Ensure that the **Federated Authentication** radio button is selected and click **Update** to save the changes.
    
Now you have added and configured the service provider.

### Configuring claims

For more information, see [Adding Claim Mapping](https://docs.wso2.com/display/IS530/Adding+Claim+Mapping) in WSO2 IS guide.

1. Sign in to the [Management Console](https://docs.wso2.com/display/IS530/Getting+Started+with+the+Management+Console) by entering your username and password.
2. In the **Main** menu, click **Add** under Claims.
3. Click **Add Claim Dialect** to create the Nuxeo authenticator specific claim dialect.
4. Specify the Dialect URI as follows:  http://wso2.org/nuxeo/claims  
5. Click **Add** to create the claim dialect.

    ![alt text](images/im8.png)
    
6. Map a new external claim to an existing local claim dialect.

   You need to map at least one claim under this new dialect. Therefore, let's map the claim for last name.
   
   1. In the **Main** menu, click **Add** under Claims.
   2. Click **Add External Claim** to add a new claim to the Nuxeo claim dialect.
   3. Select the Dialect URI as - http://wso2.org/nuxeo/claims
   4. Enter the External Claim URI based on the following claim mapping information.
   5. Select the Mapped Local Claim based on the following claim mapping information.
   
        Claim mapping for last name 
        
        <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
        <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/lastName</td> </tr>
        <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/lastname</td> </tr>
        </tbody> </table>
        
   6. Click **Add** to add the new external claim.
   
        ![alt text](images/im9.png)
        
7. Similarly, you can create claims for all the public information of the Nuxeo user by repeating step 6 with the following claim mapping information.
    
    Claim mapping for first name 
    
    <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
    <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/firstName</td> </tr>
    <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/givenname</td> </tr>
    </tbody> </table>
    
    Claim mapping for email 
    
    <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
    <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/email</td> </tr>
    <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/emailaddress</td> </tr>
    </tbody> </table>
            
    Claim mapping for groups 
    
    <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
    <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/groups</td> </tr>
    <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/role</td> </tr>
    </tbody> </table>
            
    Claim mapping for user id 
    
    <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
    <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/id</td> </tr>
    <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/userid</td> </tr>
    </tbody> </table>
            
    Claim mapping for extended group 
    
    <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
    <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/extendedGroups</td> </tr>
    <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/group</td> </tr>
    </tbody> </table>
            
    Claim mapping for user name 
    
    <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
    <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/username</td> </tr>
    <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/username</td> </tr>
    </tbody> </table>
    
    Claim mapping for entity-type 
        
    <table> <tbody> <tr> <td><b>Dialect URI</b></td> <td>http://wso2.org/nuxeo/claims</td> </tr>
    <tr> <td><b>External Claim URI</b></td> <td>http://wso2.org/nuxeo/claims/entity-type</td> </tr>
    <tr> <td><b>Mapped Local Claim</b></td> <td>http://wso2.org/claims/userType</td> </tr>
    </tbody> </table>
      
8. Click **Update**.

### Configuring requested claims for travelocity.com

1. In the **Identity** section under the **Main** tab, click **List** under **Service Providers**.
2. Click **Edit** to edit the travelocity.com service provider.
3. Go to **Claim Configuration**.
4. Click on **Add Claim URI** under **Requested Claims** to add the requested claims as follows. 
5. Select the Subject Claim URI as  http://wso2.org/claims/username to define the authenticated user identifier that will return with the authentication response to the service provider.
    
    ![alt text](images/im13.png)
    
6. Click **Update** to save your service provider changes.

### Testing the sample

1. To test the sample, navigate to the following URL: http://<TOMCAT_HOST>:<TOMCAT_PORT>/travelocity.com/index.jsp. 
    
    E.g., [http://localhost:8080/travelocity.com](http://localhost:8080/travelocity.com)

2. Click the link to log in with SAML from the WSO2 Identity Server.
    
    ![alt text](images/im5.png)

3. Enter your Nuxeo credentials in the prompted login page of Nuxeo. Once you log in successfully you will be taken to the homepage of the travelocity.com app.
    
    ![alt text](images/im11.png)
   
Now that you understand how to use Nuxeo as a federated authenticator with WSO2 Identity Server, you can configure the Nuxeo authenticator as required to authenticate Nuxeo users to log in to your organization’s applications.

