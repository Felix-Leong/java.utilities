package de.ebf.utils.auth.ldap;

import de.ebf.utils.Config;

public class LdapDefaultConfig {

   private static String server = Config.instance.getString("ldap.host");
   private static Integer port = Integer.parseInt(Config.instance.getString("ldap.port"));
   private static String context = Config.instance.getString("ldap.context");
   private static String user = Config.instance.getString("ldap.user");
   private static String pass = Config.instance.getString("ldap.pass");
   private static LdapType type;
   
   static {
       String ldapType = Config.instance.getString("ldap.type");
       if (ldapType == null){
           throw new RuntimeException("missing required config entry ldap.type");
       }
       type = LdapType.valueOf(ldapType);
   }
    

   public static String getServer() {
      return server;
   }

   public static void setServer(String server) {
      LdapDefaultConfig.server = server;
   }

   public static String getContext() {
      return context;
   }

   public static void setContext(String context) {
      LdapDefaultConfig.context = context;
   }

   public static String getUser() {
      return user;
   }

   public static void setUser(String user) {
      LdapDefaultConfig.user = user;
   }

   public static String getPass() {
      return pass;
   }

   public static void setPass(String pass) {
      LdapDefaultConfig.pass = pass;
   }

   public static Integer getPort() {
      return port;
   }

   public static void setPort(Integer aPort) {
      port = aPort;
   }

    public static LdapType getType() {
        return type;
    }

    public static void setType(LdapType type) {
        LdapDefaultConfig.type = type;
    }
}
