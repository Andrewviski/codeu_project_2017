package codeu.chat.codeU_db;

public class CreateDatabase
{

    public static void main( String args[] )
    {
        DataBaseConnection connection = new DataBaseConnection();
        connection.createTables();
        System.out.println("Finished Creating Database Successfully!!!");
    }
}