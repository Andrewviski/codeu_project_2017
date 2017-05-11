package codeu.chat.codeU_db;

import java.sql.*;
import java.util.Optional;

public final class DataBaseConnection{
    private static Connection c = null;
    private static Statement stmt = null;

    public DataBaseConnection()
    {
        c = null;
        stmt = null;
    }

    public static Connection open(){
        if(c!=null){
            System.out.println("ERROR: already connected to the Database");
        }else {
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:./bin/codeu/chat/codeU_db/ChatDatabase.db");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }
        return c;
    }
    public static void createTables(){
        c = null;
        open();
        try {
            stmt =c.createStatement();
            String sql = "CREATE TABLE USERS " +
                    "(ID            VARCHAR(16) PRIMARY KEY NOT NULL," +
                    " UNAME         CHAR(25)    UNIQUE      NOT NULL, " +
                    " TimeCreated   BIGINT               NOT NULL, " +
                    " PASSWORD      TEXT                    NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table <USERS> created successfully");

        try {
            stmt =c.createStatement();
            String sql = "CREATE TABLE CONVERSATIONS " +
                    "(ID            VARCHAR(16) PRIMARY KEY NOT NULL, " +
                    " CNAME         CHAR(25)                NOT NULL, " +
                    " OWNERID       VARCHAR(16)             NOT NULL, " +
                    " TimeCreated   BIGINT               NOT NULL, " +

                    " FOREIGN KEY(OWNERID) REFERENCES USERS(ID))";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table <CONVERSATIONS> created successfully");

        try {
            stmt =c.createStatement();
            String sql = "CREATE TABLE USER_CONVERSATION " +

                    "(ID                VARCHAR(32) PRIMARY KEY NOT NULL, " +
                    " USERID            VARCHAR(16)             NOT NULL, " +
                    " CONVERSATIONID    VARCHAR(16)             NOT NULL, " +
                    " FOREIGN KEY(USERID)         REFERENCES USERS(ID), " +
                    " FOREIGN KEY(CONVERSATIONID) REFERENCES CONVERSATIONS(ID))";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table <USER_CONVERSATION> created successfully");

        try {
            stmt =c.createStatement();
            String sql = "CREATE TABLE MESSAGES " +
                    "(ID                VARCHAR(16) PRIMARY KEY NOT NULL, " +
                    " USERID            VARCHAR(16)             NOT NULL, " +
                    " MNEXTID           VARCHAR(16),                      " +
                    " MPREVID           VARCHAR(16),                      " +
                    " CONVERSATIONID    VARCHAR(16)             NOT NULL, " +
                    " TimeCreated       BIGINT                  NOT NULL, " +
                    " MESSAGE           TEXT                    NOT NULL, " +
                    " FOREIGN KEY(USERID)         REFERENCES USERS(ID), " +
                    " FOREIGN KEY(MNEXTID)        REFERENCES MESSAGES(ID), " +
                    " FOREIGN KEY(MPREVID)        REFERENCES MESSAGES(ID), " +
                    " FOREIGN KEY(CONVERSATIONID) REFERENCES CONVERSATIONS(ID))";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table <MESSAGES> created successfully");
        close();
    }
    public static void dropTables(){
        Statement stmt = null;
        //drop tables before creating them
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE USERS";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "DROP TABLE CONVERSATIONS";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "DROP TABLE USER_CONVERSATION";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "DROP TABLE MESSAGES";
            stmt.executeUpdate(sql);
            stmt.close();

            c.commit();
        }catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public static Connection getConnection(){
        return c;
    }

    public static boolean dbUpdate(String str)
    {
        open();
        boolean status = true;
        try {
            stmt = c.createStatement();
            stmt.executeUpdate(str);
            stmt.close();
            c.commit();
        } catch (Exception e) {
            System.out.println("Error adding message to conversation");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            status = false;
            System.exit(0);
        }
        close();
        return status;
    }

    public static ResultSet dbQuery(String str)
    {
        ResultSet query = null;
        open();
        try {

            Statement stmt = c.createStatement();
            query = stmt.executeQuery(str);

            while (query.next())
            {
                System.out.println(query.getString("UNAME"));
            }

            stmt.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        close();
        return query;
    }

    public static void close(){
        try {
            c.close();
            c=null;
        }catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}