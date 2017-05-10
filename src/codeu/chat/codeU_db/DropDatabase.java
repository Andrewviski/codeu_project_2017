package codeu.chat.codeU_db;

import java.sql.DatabaseMetaData;

public class DropDatabase
{
    public static void main( String args[] )
    {
        DataBaseConnection.open();
        DataBaseConnection.dropTables();
        System.out.println("Finished Droping Database Successfully!!!");
        DataBaseConnection.close();
    }
}