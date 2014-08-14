/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.onpremise;

import de.ebf.utils.mail.SMTPMailConfig;
import de.ebf.db.DBUtil;
import de.ebf.db.DBType;
import de.ebf.db.DBConfig;
import de.ebf.utils.Bundle;
import de.ebf.utils.FormUtils;
import de.ebf.utils.mail.MailUtils;
import de.ebf.utils.auth.ldap.LdapType;
import de.ebf.utils.auth.ldap.LdapUtil;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author xz
 */
@Controller
@RequestMapping("/")
public class SetupWizardController {

    private static final Logger log = Logger.getLogger(SetupWizardController.class);
    private static final String SESSION_ATTR_DBCONFIG = "dbConfig";
    private static final String SESSION_ATTR_LDAPCONFIG = "ldapConfig";
    private static final String SESSION_ATTR_MAILCONFIG = "mailConfig";
    
    
    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView handleSetupStarting(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("onpremise/setupwizard");
        modelAndView.addObject("DBTypes", DBType.values());
        modelAndView.addObject("ldapTypes", LdapType.values());
        return modelAndView;
    }

    @RequestMapping("start")
    @ResponseBody
    protected SetupWizardMessage start(){
        return new SetupWizardMessage(SetupWizardMessage.SUCCESS, null);
    }
    
    @RequestMapping("configDBSubmit")
    @ResponseBody
    protected SetupWizardMessage handleConfigDBSubmit(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession(true);
        //remove this session attribute if any.
        session.removeAttribute(SESSION_ATTR_DBCONFIG);
        DBConfig dbConfig;

        try {
            dbConfig = validateDBParameters(request);
        } catch (Exception e) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR, e.getMessage());
        }

        //test the db connection and set the messageDetail
        try {
            DBUtil.testDB(dbConfig);
        } catch (Exception e) {
            SetupWizardMessage msg =  new SetupWizardMessage(SetupWizardMessage.ERROR,Bundle.getString("SetupWizardMessage_DB_CONN_FAILED")+" "+e.getMessage());
            return msg;
        }

        //if successful, put this attribute in session object
        session.setAttribute(SESSION_ATTR_DBCONFIG, dbConfig);
        return new SetupWizardMessage(SetupWizardMessage.SUCCESS, null);
    }

    @RequestMapping("configLdapSubmit")
    @ResponseBody
    protected SetupWizardMessage handleConfigLdapSubmit(HttpServletRequest request, HttpServletResponse response){

        HttpSession session = request.getSession(false);
        if (session == null) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR,Bundle.getString("SetupWizardMessage_SESSION_NOT_EXISTING"));
        }
        //remove this session attribute if any.
        session.removeAttribute(SESSION_ATTR_LDAPCONFIG);

        try {
            LdapUtil.validateLdapParameters(request);
        } catch (Exception e) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR,e.getMessage());
        }
        
        String ldapTypeString = request.getParameter("ldapType");
        String ldapServer = request.getParameter("ldapServer");
        String ldapPortString = request.getParameter("ldapPort");
        String ldapServer2 = request.getParameter("ldapServer2");
        String ldapPortString2 = request.getParameter("ldapPort2");
        String ldapUser = request.getParameter("ldapUser");
        String ldapPass = request.getParameter("ldapPass");
        String ldapBaseDN = request.getParameter("ldapBaseDN");
        LdapType ldapType = LdapType.valueOf(ldapTypeString);
        Integer ldapPort = Integer.parseInt(ldapPortString);
        
        LdapConfig config = new LdapConfig();
        config.setType(ldapType);
        config.setServer(ldapServer);
        config.setPort(ldapPort);
        config.setUsername(ldapUser);
        config.setPassword(ldapPass);
        config.setBaseDN(ldapBaseDN);
      
        if (!StringUtils.isEmpty(ldapServer2) && !StringUtils.isEmpty(ldapPortString)){
            config.setServer2(ldapServer2);
            Integer ldapPort2 = Integer.parseInt(ldapPortString2);
            config.setPort2(ldapPort2);
        }
        
        //verify the ldap connection
        try {
            LdapUtil.verifyConnection(config);
        } catch (Exception e) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR, Bundle.getString("SetupWizardMessage_LDAP_CONN_FAILED")+" "+e.getMessage());
        }
        
        //if successful, put this attribute in session object
        session.setAttribute(SESSION_ATTR_LDAPCONFIG, config);
        return new SetupWizardMessage(SetupWizardMessage.SUCCESS, null);
    }
    
    @RequestMapping("configMailSubmit")
    @ResponseBody
    protected SetupWizardMessage handleConfigMailSubmit(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR,Bundle.getString("SetupWizardMessage_SESSION_NOT_EXISTING"));
        }
        //remove this session attribute if any.
        session.removeAttribute(SESSION_ATTR_MAILCONFIG);

        //validate and return the filled mailConfig
        SMTPMailConfig mailConfig;
        try {
            mailConfig = validateMailParameters(request);
        } catch (Exception e) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR,e.getMessage());
        }

        //send testing email to verify the mail properties
        try {
            sendTestingEmail(mailConfig);
        } catch (Exception e) {
            String msg = Bundle.getString("SetupWizardMessage_MAIL_SENDING_FAILED") + " "+e;
            if (!StringUtils.isEmpty(e.getMessage())){
                msg+= " "+e.getMessage();
            }
            log.error("Failed to send email", e);
            return new SetupWizardMessage(SetupWizardMessage.ERROR,msg);
        }

        //if mail testing is successful, put this attribute in session object
        session.setAttribute(SESSION_ATTR_MAILCONFIG, mailConfig);

        return new SetupWizardMessage(SetupWizardMessage.SUCCESS, null);
    }
    
    @RequestMapping("finish")
    @ResponseBody
    protected SetupWizardMessage finish(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR,Bundle.getString("SetupWizardMessage_SESSION_NOT_EXISTING"));
        }        
        //check if all the properties object are stored in session
        DBConfig dbConfig = (DBConfig) session.getAttribute(SESSION_ATTR_DBCONFIG);
        LdapConfig ldapConfig = (LdapConfig) session.getAttribute(SESSION_ATTR_LDAPCONFIG);
        SMTPMailConfig mailConfig = (SMTPMailConfig) session.getAttribute(SESSION_ATTR_MAILCONFIG);
        if(dbConfig==null || ldapConfig==null || mailConfig==null) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR,Bundle.getString("SetupWizardMessage_SESSION_NOT_EXISTING"));
        }
        
        try {
            // update the properties files in ROOT by replacing the variables with the collected data from the user.
            writePropertiesFiles(dbConfig, ldapConfig, mailConfig);
        } catch (IOException ex) {
            return new SetupWizardMessage(SetupWizardMessage.ERROR, ex.getMessage());
        }
        
        //copy the updated properties files from ROOT or classes directory to virtual host directory for later use.
        OnpremiseUtil.copyPropertiesFileFromRootToVirtualHostDir();

        //restart the app to make the properties take effect
        OnpremiseUtil.restartWebApplication();
        
        return new SetupWizardMessage(SetupWizardMessage.SUCCESS, null);
    }

    
    private DBConfig validateDBParameters(HttpServletRequest request) throws Exception {
        String typeString = request.getParameter("type");
        String host = request.getParameter("host");
        String portString = request.getParameter("port");
        String dbName = request.getParameter("dbName");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");

        DBType dbType = DBType.valueOf(typeString);

        Integer port = Integer.parseInt(portString);

        if (StringUtils.isEmpty(dbName) || StringUtils.isEmpty(host) || StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            throw new Exception(Bundle.getString("RequiredFieldsMustNotBeEmpty"));
        }
        //if no error, fill the fields
        DBConfig dbConfig = new DBConfig();
        dbConfig.setType(dbType);
        dbConfig.setHost(host);
        dbConfig.setPort(port);
        dbConfig.setDbName(dbName);
        dbConfig.setUsername(userName);
        dbConfig.setPassword(password);

        return dbConfig;
    }
        
    /**
     * validate the mail parameters. If no eror, create a SMTPMailConfig and fill it with the parameters.
     *
     * @param request
     * @return
     * @throws Exception
     */
    private SMTPMailConfig validateMailParameters(HttpServletRequest request) throws Exception {
        String smtpProtocol = request.getParameter("smtpProtocol");
        String smtpHost = request.getParameter("smtpHost");
        String smtpPortStr = request.getParameter("smtpPort");
        String senderEmail = request.getParameter("senderEmail");
        String senderName = request.getParameter("senderName");
        String bccEmail = request.getParameter("bccEmail");
        String requireAuthStr = request.getParameter("requireAuth");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        boolean requireAuth = (!StringUtils.isEmpty(requireAuthStr) && requireAuthStr.equals("on"));

        if (StringUtils.isEmpty(smtpProtocol) || StringUtils.isEmpty(smtpHost) || StringUtils.isEmpty(senderName) || StringUtils.isEmpty(senderEmail)) {
            throw new Exception(Bundle.getString("RequiredFieldsMustNotBeEmpty"));
        }
        if (requireAuth) {
            if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(userName)) {
                throw new Exception(Bundle.getString("RequiredFieldsMustNotBeEmpty"));
            }
        }
        Integer smtpPort = Integer.parseInt(smtpPortStr);
        if (!FormUtils.isEmail(senderEmail) || !FormUtils.isEmail(bccEmail)) {
            throw new Exception(Bundle.getString("EmailAddressInvalid"));
        }

        //if no error, fill the fields
        SMTPMailConfig mailConfig = new SMTPMailConfig();
        mailConfig.setProtocol(smtpProtocol);
        mailConfig.setHost(smtpHost);
        mailConfig.setPort(smtpPort);
        mailConfig.setSenderEmail(senderEmail);
        mailConfig.setBccEmail(bccEmail);
        mailConfig.setSenderName(senderName);
        mailConfig.setRequireAuthentication(requireAuth);
        if (requireAuth) {
            mailConfig.setUsername(userName);
            mailConfig.setPassword(password);
        } else {
            mailConfig.setUsername("");
            mailConfig.setPassword("");
        }

        return mailConfig;
    }

    /**
     * send the testing email to test if the mail settings are correct
     * @param mailConfig
     * @throws Exception 
     */
    private void sendTestingEmail(SMTPMailConfig mailConfig) throws Exception {
        String sender = mailConfig.getSenderEmail();
        ArrayList<String> recipients = new ArrayList<>();
        recipients.add(mailConfig.getBccEmail());
        String subject = "Testing email to verify the mail properties";
        String body = "This email is sent to verify the mail properties entered in onpremise-SetupWizard.";

        //send testing mail, throws exception in case of errors
        MailUtils.sendMail(mailConfig, sender, recipients, subject, body);
    }

    /**
     * write the entered properties into the properties files
     *
     * @param dbConfig
     * @param ldapConfig
     * @param mailConfig
     * @return
     * @throws IOException
     */
    private boolean writePropertiesFiles(DBConfig dbConfig, LdapConfig ldapConfig, SMTPMailConfig mailConfig) throws IOException {
        HashMap<String, String> localSettingsReplaceMap = new HashMap<>();
        HashMap<String, String> jdbcReplaceMap = new HashMap<>();

        //db
        jdbcReplaceMap.put("${db.driverClassName}", dbConfig.getDatabaseType().getDriverClass());
        jdbcReplaceMap.put("${db.dialect}", dbConfig.getDatabaseType().getDialect());
        jdbcReplaceMap.put("${db.databaseurl}", dbConfig.getUrl());
        jdbcReplaceMap.put("${db.username}", dbConfig.getUsername());
        jdbcReplaceMap.put("${db.password}", dbConfig.getPassword());

        //ldap
        localSettingsReplaceMap.put("${ldap.type}", ldapConfig.getType().name());
        localSettingsReplaceMap.put("${ldap.host}", ldapConfig.getServer());
        localSettingsReplaceMap.put("${ldap.port}", ldapConfig.getPort().toString());
        localSettingsReplaceMap.put("${ldap.context}", ldapConfig.getBaseDN());
        localSettingsReplaceMap.put("${ldap.user}", ldapConfig.getUsername());
        localSettingsReplaceMap.put("${ldap.pass}", ldapConfig.getPassword());
        
        if (!StringUtils.isEmpty(ldapConfig.getServer2()) && ldapConfig.getPort2()!=null){
            localSettingsReplaceMap.put("${ldap.host2}", ldapConfig.getServer2());
            localSettingsReplaceMap.put("${ldap.port2}", ldapConfig.getPort2().toString());
        }

        //mail
        localSettingsReplaceMap.put("${smtp.protocol}", mailConfig.getProtocol());
        localSettingsReplaceMap.put("${smtp.server}", mailConfig.getHost());
        localSettingsReplaceMap.put("${smtp.port}", mailConfig.getPort() + "");
        localSettingsReplaceMap.put("${smtp.user}", mailConfig.getUsername());
        localSettingsReplaceMap.put("${smtp.pass}", mailConfig.getPassword());
        localSettingsReplaceMap.put("${smtp.user.mail}", mailConfig.getSenderEmail());
        localSettingsReplaceMap.put("${smtp.user.name}", mailConfig.getSenderName());
        localSettingsReplaceMap.put("${smtp.bcc.mail}", mailConfig.getBccEmail());

        replaceVariablesInFile(ImportantFile.LOCAL_SETTINGS_PROPERTIES_FILE_IN_ROOT.getFilePath(), localSettingsReplaceMap);
        replaceVariablesInFile(ImportantFile.JDBC_PROPERTIES_FILE_IN_ROOT.getFilePath(), jdbcReplaceMap);
        return true;
    }

    /**
     * replace the property variables in the specified file with the property value specified in the HashMap. 
     */
    private void replaceVariablesInFile(String filePathStr, HashMap<String, String> replaceMap) throws IOException {
        Path path = Paths.get(filePathStr);
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        for (Map.Entry<String, String> entrySet : replaceMap.entrySet()) {
            content = content.replace(entrySet.getKey(), entrySet.getValue());
        }
        Files.write(path, content.getBytes(charset));
    }

}
