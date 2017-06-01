package codeu.chat.codeU_db;

public class CreateDatabase {

  public static void main(String args[]) {
    DataBaseConnection connection = new DataBaseConnection();
    connection.createTables();
    System.out.println("Finished Creating Database Successfully!!!");
  }
public class CreateDatabase
{
    public static void main( String args[] )
    {
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./bin/codeu/chat/codeU_db/ChatDatabase.db");
            System.out.println("Opened database successfully");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        try {
            stmt = c.createStatement();
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
            stmt = c.createStatement();
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
            stmt = c.createStatement();
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
            stmt = c.createStatement();
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

            c.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table <MESSAGES> created successfully");

        System.out.println("Finished Creating Database Successfully!!!");
    }
}