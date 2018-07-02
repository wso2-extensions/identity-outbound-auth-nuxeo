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

package org.wso2.carbon.identity.authenticator.nuxeo.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.authenticator.nuxeo.NuxeoAuthenticator;

/**
 * OSGi declarative services component which handled registration and unregistration of NuxeoServiceComponent.
 */
@Component(
        name = "identity.application.authenticator.nuxeo.component",
        immediate = true)
public class NuxeoAuthenticatorServiceComponent {

    private static Log log = LogFactory.getLog(NuxeoAuthenticatorServiceComponent.class);
    private ServiceRegistration registration = null;

    /**
     * Activates the Registry bundle.
     *
     * @param componentContext the OSGi component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            NuxeoAuthenticator nuxeoAuthenticator = new NuxeoAuthenticator();
            registration = componentContext.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                    nuxeoAuthenticator, null);
            if (log.isDebugEnabled()) {
                log.debug("Nuxeo authenticator is activated.");
            }
        } catch (Exception e) {
            log.error("Error while activating the Nuxeo authenticator. ", e);
        }
    }

    /**
     * Deactivates the Registry bundle.
     *
     * @param componentContext the OSGi component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        registration.unregister();
        registration = null;
        if (log.isDebugEnabled()) {
            log.debug("Nuxeo authenticator bundle is de-activated.");
        }
    }
}
