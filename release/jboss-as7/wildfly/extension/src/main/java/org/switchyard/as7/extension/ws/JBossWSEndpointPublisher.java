/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.switchyard.as7.extension.ws;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.switchyard.ServiceDomain;
import org.switchyard.component.common.Endpoint;
import org.switchyard.component.soap.InboundHandler;
import org.switchyard.component.soap.WebServicePublishException;
import org.switchyard.component.soap.config.model.SOAPBindingModel;
import org.switchyard.component.soap.endpoint.AbstractEndpointPublisher;

import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles publishing of Webservice Endpoints on JBossWS stack.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class JBossWSEndpointPublisher extends AbstractEndpointPublisher {

    private static final Logger LOGGER = Logger.getLogger("org.switchyard");

    private static final String SEI = "org.switchyard.component.soap.endpoint.BaseWebService";
    private static final String RESPONSE_STATUS_HANDLER = "ResponseStatusHandler";

    /**
     * {@inheritDoc}
     */
    public synchronized Endpoint publish(ServiceDomain domain, final SOAPBindingModel config, final String bindingId, final InboundHandler handler, WebServiceFeature... features) {
        JBossWSEndpoint wsEndpoint = null;
        try {
            initialize(config);
            Map<String,String> map = new HashMap<String, String>();
            map.put("/" + config.getPort().getServiceName(), SEI);

            boolean addrFeatureEnabled = false;
            boolean addrFeatureRequired = false;
            String addrFeatureType = "ANONYMOUS";
            boolean mtomFeatureEnabled = false;
            int mtomFeatureThreshold = 0;

            boolean respectFeatureEnable = false;

            for (WebServiceFeature feature : features) {
                if (feature instanceof AddressingFeature) {
                    AddressingFeature addrFeature = (AddressingFeature)feature;
                    addrFeatureEnabled = addrFeature.isEnabled();
                    addrFeatureRequired = addrFeature.isRequired();
                    addrFeatureType = addrFeature.getResponses() == null ? addrFeatureType : addrFeature.getResponses().name();
                    LOGGER.info("Addressing [enabled = " + addrFeature.isEnabled() + ", required = " + addrFeature.isRequired() + "]");
                } else if (feature instanceof MTOMFeature) {
                    MTOMFeature mtom = (MTOMFeature)feature;
                    mtomFeatureEnabled = mtom.isEnabled();
                    mtomFeatureThreshold = mtom.getThreshold();
                    LOGGER.info("MTOM [enabled = " + mtom.isEnabled() + ", threshold = " + mtom.getThreshold() + "]");
                } else if (feature instanceof RespectBindingFeature) {
                    respectFeatureEnable = feature.isEnabled();
                }
            }


            String portComponentName = config.getServiceName()
                    + ":" + config.getPort().getServiceQName().getLocalPart()
                    + ":" + config.getPort().getPortQName().getLocalPart();

            PortComponentMetaData portComponent = new PortComponentMetaData(
                    portComponentName,
                    config.getPort().getPortQName(),
                    SEI,
                    null,
                    config.getPort().getServiceQName().getLocalPart(),
                    null,
                    getContextRoot(),
                    addrFeatureEnabled,
                    addrFeatureRequired,
                    addrFeatureType,
                    mtomFeatureEnabled,
                    mtomFeatureThreshold,
                    respectFeatureEnable,
                    config.getPort().getServiceQName(),
                    null,
                    null
                    );

            WebserviceDescriptionMetaData wsDescMetaData = new WebserviceDescriptionMetaData(config.getName(), getWsdlLocation(), null, portComponent);
            WebservicesMetaData wsMetadata = new WebservicesMetaData(null, wsDescMetaData);

            wsEndpoint = new JBossWSEndpoint();
            if (config.getContextPath() != null) {
                wsEndpoint.publish(domain, getContextRoot(), map, wsMetadata, config, handler);
            } else {
                wsEndpoint.publish(domain, getContextPath(), map, wsMetadata, config, handler);
            }
        } catch (Exception e) {
            throw new WebServicePublishException(e);
        }
        return wsEndpoint;
    }
}
