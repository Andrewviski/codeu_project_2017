package codeu.chat.codeU_db;

public class ResetDatabase {
  public static void main(String args[]) {
    DataBaseConnection connection = new DataBaseConnection();
    connection.dropTables();
    connection.createTables();
    System.out.println("Finished Resetting Database Successfully!!!");
  }
}