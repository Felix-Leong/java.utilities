/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap.schema;

import de.ebf.utils.auth.ldap.LdapType;
import static de.ebf.utils.auth.ldap.LdapType.ActiveDirectory;
import static de.ebf.utils.auth.ldap.LdapType.OpenDS;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dominik
 */
public class LdapSchemaFactory {

    private final static Map<LdapType, LdapSchema> instances = new HashMap<>();

    public static LdapSchema getLdapSchema(LdapType type) {
        if (instances.containsKey(type)) {
            return instances.get(type);
        }

        switch (type) {
            case ActiveDirectory:
                instances.put(type, new ActiveDirectorySchema());
                break;
            case OpenDS:
                instances.put(type, new OpenDSSchema());
                break;
        }
        return instances.get(type);
    }
}
