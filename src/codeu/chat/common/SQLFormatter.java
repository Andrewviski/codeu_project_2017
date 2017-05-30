package codeu.chat.common;

import codeu.chat.util.Uuid;
import codeu.chat.util.Time;

/**
 * Created by Jose Zavala on 4/04/17.
 */
public class SQLFormatter {
  public static String sqlID(Uuid userID) {
    String sqlID = "NULL";
    if (userID != null) {
      sqlID = userID.toString();
      sqlID = sqlID.replace("[UUID:", "");
      sqlID = sqlID.replace("]", "");
    }

    return sqlID;
  }

  public static String sqlID(Uuid userID1, Uuid userID2) {
    String sqlID1 = "NULL";
    if (userID1 != null) {
      sqlID1 = userID1.toString();
      sqlID1 = sqlID1.replace("[UUID:", "");
      sqlID1 = sqlID1.replace("]", "");
    }

    String sqlID2 = "NULL";
    if (userID2 != null) {
      sqlID2 = userID2.toString();
      sqlID2 = sqlID2.replace("[UUID:", "");
      sqlID2 = sqlID2.replace("]", "");
    }

    return sqlID1 + sqlID2;
  }

  public static String sqlCreationTime(Time userTime) {
    Long inMs = userTime.inMs();
    String sqlCreationTime = Long.toString(inMs);
    return sqlCreationTime;
  }
}