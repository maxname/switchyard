package org.switchyard.security.jboss.bindings.mapping;

import org.jboss.logging.Logger;
import org.jboss.security.identity.RoleGroup;
import org.jboss.security.identity.plugins.SimpleRole;
import org.jboss.security.identity.plugins.SimpleRoleGroup;
import org.jboss.security.mapping.MappingProvider;
import org.jboss.security.mapping.MappingResult;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Zinoviev Oleg on 17.02.2016.
 */
public class CustomSTSGroupMappingProvider  implements MappingProvider<RoleGroup> {
    private static final Logger logger = Logger.getLogger(CustomSTSGroupMappingProvider.class);
    private MappingResult<RoleGroup> result;
    private String tokenRoleAttributeName;

    public CustomSTSGroupMappingProvider() {
    }

    public void init(Map<String, Object> contextMap) {
        Object tokenRoleAttributeObject = contextMap.get("token-role-attribute-name");
        if(tokenRoleAttributeObject != null) {
            this.tokenRoleAttributeName = (String)tokenRoleAttributeObject;
        } else {
            this.tokenRoleAttributeName = "role";
        }

        logger.trace("Initialized with " + contextMap);
    }

    public void performMapping(Map<String, Object> contextMap, RoleGroup Group) {
        logger.debug("performMapping with map as " + contextMap);
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
            AttributeStatementType attributeStatement = this.getAttributeStatement(assertion);
            if(attributeStatement != null) {
                SimpleRoleGroup rolesGroup = new SimpleRoleGroup("Roles");
                List attributeList = attributeStatement.getAttributes();
                Iterator i$ = attributeList.iterator();

                while(true) {
                    AttributeType attribute;
                    do {
                        do {
                            if(!i$.hasNext()) {
                                this.result.setMappedObject(rolesGroup);
                                logger.trace("Mapped roles to " + rolesGroup);
                                return;
                            }

                            AttributeStatementType.ASTChoiceType obj = (AttributeStatementType.ASTChoiceType)i$.next();
                            attribute = obj.getAttribute();
                        } while(attribute == null);
                    } while(!this.tokenRoleAttributeName.equals(attribute.getName()));

                    Iterator i$1 = attribute.getAttributeValue().iterator();

                    while(i$1.hasNext()) {
                        Object value = i$1.next();
                        rolesGroup.addRole(new SimpleRole((String)value));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse SAML Assertion", e);
        }

    }

    public void setMappingResult(MappingResult<RoleGroup> mappingResult) {
        this.result = mappingResult;
    }

    public boolean supports(Class<?> p) {
        return RoleGroup.class.isAssignableFrom(p);
    }

    private AttributeStatementType getAttributeStatement(AssertionType assertion) {
        Set statementList = assertion.getStatements();
        if(statementList.size() != 0) {
            Iterator i$ = statementList.iterator();

            while(i$.hasNext()) {
                StatementAbstractType statement = (StatementAbstractType)i$.next();
                if(statement instanceof AttributeStatementType) {
                    return (AttributeStatementType)statement;
                }
            }
        }

        return null;
    }
}
