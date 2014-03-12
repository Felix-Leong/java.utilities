package de.ebf.utils.auth.ldap.config;

import de.ebf.utils.Config;
import de.ebf.utils.auth.ldap.LdapType;

public class LdapDefaultConfig extends LdapConfig {

    private static LdapDefaultConfig instance;

    private LdapDefaultConfig() {
        //hide constructor
    }

    public static LdapDefaultConfig getInstance() {
        if (instance == null) {
            instance = new LdapDefaultConfig();
            String ldapType = Config.instance.getString("ldap.type");
            LdapType type;
            try {
                type = LdapType.valueOf(ldapType);
            } catch (IllegalArgumentException e){
                throw new RuntimeException("missing or invalid config entry ldap.type", e);
            }
            instance.setType(type);
            instance.setServer(Config.instance.getString("ldap.host"));
            instance.setPort(Integer.parseInt(Config.instance.getString("ldap.port")));
            instance.setBaseDN(Config.instance.getString("ldap.context"));
            instance.setUsername(Config.instance.getString("ldap.user"));
            instance.setPassword(Config.instance.getString("ldap.pass"));
        }
        return instance;
    }
}
