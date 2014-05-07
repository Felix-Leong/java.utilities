package de.ebf.utils.auth.ldap.config;

import de.ebf.utils.Config;
import de.ebf.utils.auth.ldap.LdapType;

public class LdapDefaultConfig extends LdapConfig {

    private static final long serialVersionUID = 1L;

    private static LdapDefaultConfig instance;
    
    private LdapDefaultConfig() {
        //hide constructor
    }

    public static synchronized LdapDefaultConfig getInstance() {
        if (instance == null) {
            instance = new LdapDefaultConfig();
            String ldapType = Config.getInstance().getString("ldap.type");
            LdapType type;
            try {
                type = LdapType.valueOf(ldapType);
            } catch (IllegalArgumentException e){
                throw new RuntimeException("missing or invalid config entry ldap.type", e);
            }
            instance.setType(type);
            instance.setServer(Config.getInstance().getString("ldap.host"));
            instance.setPort(Integer.parseInt(Config.getInstance().getString("ldap.port")));
            instance.setBaseDN(Config.getInstance().getString("ldap.context"));
            instance.setUsername(Config.getInstance().getString("ldap.user"));
            instance.setPassword(Config.getInstance().getString("ldap.pass"));
        }
        return instance;
    }
}
