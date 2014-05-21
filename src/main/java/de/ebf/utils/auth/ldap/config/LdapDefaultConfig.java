package de.ebf.utils.auth.ldap.config;

import de.ebf.utils.Config;
import de.ebf.utils.auth.ldap.LdapType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class LdapDefaultConfig extends LdapConfig {
    
    private static final Logger log = Logger.getLogger(LdapDefaultConfig.class);

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
            
            //fallback LDAP server
            String host2 = Config.getInstance().getString("ldap.host2");
            String port2 = Config.getInstance().getString("ldap.port2");
            if (!StringUtils.isEmpty(host2)){
                instance.setServer2(host2);
            }
            try {
                if (!StringUtils.isEmpty(port2)){
                    instance.setPort2(Integer.parseInt(port2));
                }
            } catch (NumberFormatException | NullPointerException ex){
                log.warn("Unable to parse '"+port2+"' as Integer", ex);
            }
        }
        return instance;
    }
}
