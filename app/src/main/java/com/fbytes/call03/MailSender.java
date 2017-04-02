package com.fbytes.call03;

import javax.activation.DataHandler;   
import javax.activation.DataSource;   
import javax.mail.Message;   
import javax.mail.PasswordAuthentication;   
import javax.mail.Session;   
import javax.mail.Transport;   
import javax.mail.internet.InternetAddress;   
import javax.mail.internet.MimeMessage;   
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;   
import java.io.IOException;   
import java.io.InputStream;   
import java.io.OutputStream;   
import java.io.UnsupportedEncodingException;
import java.security.Security;   
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;   

public class MailSender extends javax.mail.Authenticator {  
	
	private String TAG="MailSender";
    private String mailhost = "smtp.gmail.com";   
    private String user;   
    private String password;   
    private boolean useSSL;
    private String senderEmail;
    private String subject;
    private Session session;   
    
    static {   
        Security.addProvider(new JSSEProvider());   
    }
    


    public MailSender() {   
        
        mailhost = Call4Help.config.getString("SMTPServer","");
        user=Call4Help.config.getString("SMTPUser","");;   
        password=Call4Help.config.getString("SMTPPass","");   
        useSSL=Call4Help.config.getBoolean("SMTPUseSSL", false);       
        senderEmail=Call4Help.config.getString("SMTPSenderEmail", "");   
        subject=Call4Help.config.getString("SMTPEmailSubject", "");
        
        Properties props = new Properties();   
        props.setProperty("mail.transport.protocol", "smtp");   
        props.setProperty("mail.host", mailhost);   
        if (useSSL)
        	props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");            	
        if (useSSL){
        	props.put("mail.smtp.port", "465");   
        	props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");   
            props.put("mail.smtp.socketFactory.fallback", "false");          	
        }
        else{        
        	props.put("mail.smtp.port", "25");   
        	props.put("mail.smtp.socketFactory.port", "25");
        }
/*        
        SSLSocketFactory a;
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
*/         
        props.setProperty("mail.smtp.quitwait", "false");   

        //trustAllHosts();
        session = Session.getDefaultInstance(props, this);   
        //session.setDebug(true);
    }   
    
    protected PasswordAuthentication getPasswordAuthentication() {   
        return new PasswordAuthentication(user, password);   
    }   

    public synchronized boolean sendMail(String body, String recipients) throws Exception {   
        try{
        MimeMessage message = new MimeMessage(session);   
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));          
        message.setSender(new InternetAddress(senderEmail));   

        message.setSubject(subject);   
        message.setDataHandler(handler);   
        if (recipients.indexOf(',') > 0)   
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));   
        else  
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));   
        Transport mailTransport=session.getTransport();
        mailTransport.send(message);   
        }catch(Exception e){
        	e.printStackTrace();
        	return(false);
        }
        return(true);
    }   

    public class ByteArrayDataSource implements DataSource {   
        private byte[] data;   
        private String type;   

        public ByteArrayDataSource(byte[] data, String type) {   
            super();   
            this.data = data;   
            this.type = type;   
        }   

        public ByteArrayDataSource(byte[] data) {   
            super();   
            this.data = data;   
        }   

        public void setType(String type) {   
            this.type = type;   
        }   

        public String getContentType() {   
            if (type == null)   
                return "application/octet-stream";   
            else  
                return type;   
        }   

        public InputStream getInputStream() throws IOException {   
            return new ByteArrayInputStream(data);   
        }   

        public String getName() {   
            return "ByteArrayDataSource";   
        }   

        public OutputStream getOutputStream() throws IOException {   
            throw new IOException("Not Supported");   
        }   
    }   
    
    
    // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession session) {
              return true;
          }
   };


    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
              // Create a trust manager that does not validate certificate chains
              TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                              return new java.security.cert.X509Certificate[] {};
                      }

                      public void checkClientTrusted(X509Certificate[] chain,
                                      String authType) throws CertificateException {
                      }

                      public void checkServerTrusted(X509Certificate[] chain,
                                      String authType) throws CertificateException {
                      }
              } };

              // Install the all-trusting trust manager
              try {
                      SSLContext sc = SSLContext.getInstance("TLS");
                      sc.init(null, trustAllCerts, new java.security.SecureRandom());
                      HttpsURLConnection
                                      .setDefaultSSLSocketFactory(sc.getSocketFactory());
              } catch (Exception e) {
                      e.printStackTrace();
              }
      }
    
    
}  