package org.nilriri.webbibles.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

public class GMailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;

    //  static {   
    //    Security.addProvider(new org.apache.harmony.xnet.provider.jsse.JSSEProvider());   
    // }   

    public GMailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
        return new javax.mail.PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        MimeMessage message = new MimeMessage(session);
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setDataHandler(handler);
        
        if (recipients.indexOf(',') > 0)
            message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(recipients));
        else
            message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipients));
        
        Transport.send(message);
    }

    public class ByteArrayDataSource implements DataSource, javax.activation.DataSource {
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

        @Override
        public Connection getConnection() throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Connection getConnection(String s, String s1) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setLogWriter(PrintWriter printwriter) throws SQLException {
            // TODO Auto-generated method stub

        }

        @Override
        public void setLoginTimeout(int i) throws SQLException {
            // TODO Auto-generated method stub

        }
    }
}
