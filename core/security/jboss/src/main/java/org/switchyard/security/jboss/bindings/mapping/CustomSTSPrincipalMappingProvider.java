package org.switchyard.security.jboss.bindings.mapping;

import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.mapping.MappingResult;
import org.jboss.security.mapping.providers.principal.AbstractPrincipalMappingProvider;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.BaseIDAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.w3c.dom.Element;

import java.security.Principal;
import java.util.Map;

/**
 * Created by Zinoviev Oleg on 17.02.2016.
 */
public class CustomSTSPrincipalMappingProvider extends AbstractPrincipalMappingProvider {
    private static final Logger logger = Logger.getLogger(CustomSTSPrincipalMappingProvider.class);
    private MappingResult<Principal> result;

    public CustomSTSPrincipalMappingProvider() {
    }

    public void init(Map<String, Object> contextMap) {
    }

    public void performMapping(Map<String, Object> contextMap, Principal principal) {
        if(contextMap == null) {
            logger.warn("Mapping context is null");
        }

        Object tokenObject = contextMap.get("org.picketlink.identity.federation.core.wstrust.lm.stsToken");
        if(!(tokenObject instanceof Element)) {
            logger.debug("Did not find a token " + Element.class.getName() + " under " + "org.picketlink.identity.federation.core.wstrust.lm.stsToken" + " in the map");
        }

        try {
            Element e = (Element)tokenObject;
            AssertionType assertion = SAMLUtil.fromElement(e);
            SubjectType subject = assertion.getSubject();
            if(subject != null) {
                BaseIDAbstractType baseID = subject.getSubType().getBaseID();
                if(baseID != null && baseID instanceof NameIDType) {
                    NameIDType nameID = (NameIDType)baseID;
                    SimplePrincipal mappedPrincipal = new SimplePrincipal(nameID.getValue());
                    this.result.setMappedObject(mappedPrincipal);
                    logger.trace("Mapped principal = " + mappedPrincipal);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse SAML Assertion", e);
        }

    }

    public void setMappingResult(MappingResult<Principal> mappingResult) {
        this.result = mappingResult;
    }
}
