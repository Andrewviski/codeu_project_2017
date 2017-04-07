package codeu.chat.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by strobe on 4/04/17.
 */
public class SQLFormatter {
    public static String sqlID(Uuid userID){
        String sqlID = userID.toString();
        sqlID = sqlID.replace("[UUID:","");
        sqlID = sqlID.replace("]","");
        sqlID = "'" + sqlID + "'";
        return sqlID;
    }

    public static String sqlName(String userName){
        String sqlName = "'" + userName + "'";
        return sqlName;
    }

    public static String sqlCreationTime(Time userTime){
        SimpleDateFormat sqlFormatter = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
        String sqlCreationTime = sqlFormatter.format(new Date(userTime.inMs())).toString();
        sqlCreationTime = "'" + sqlCreationTime + "'";
        return sqlCreationTime;
    }

    public static String sqlPassword(String userPassword){
        String sqlPassword = "'" + userPassword + "'";
        return sqlPassword;
    }

    public static String sqlBody(String userBody){
        String sqlBody = "'" + userBody + "'";
        return sqlBody;
    }

    public boolean sqlValidConversation(Uuid userID, Uuid conversationID){
        boolean validConversation = false;

        Connection connection = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:./bin/codeu/chat/codeU_db/ChatDatabase.db");
            connection.setAutoCommit(false);

            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * " +
                    "FROM USER_CONVERSATION" +
                    "WHERE  USERID = "+sqlID(userID)+"" +
                    "AND    CONVERSATIONID = "+sqlID(conversationID)+";" );
            if(rs.next()){
                validConversation = true;
                System.out.println("Conversation exists and User is a member");
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return validConversation;
    }


}
