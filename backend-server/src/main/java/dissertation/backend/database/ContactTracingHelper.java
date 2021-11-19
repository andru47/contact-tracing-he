package dissertation.backend.database;

import dissertation.backend.JNIBridge;
import dissertation.backend.serialization.NewCaseMessage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ContactTracingHelper {
  private final static JNIBridge jniBridge = new JNIBridge();

  public static void addNewCovidCase(NewCaseMessage message) {
    String sqlCommand = "INSERT into quarantined_users(user_id, end) VALUES(?,?)";
    Controller.addNewInfectedUser(sqlCommand, message.getUser_id(), Long.parseLong(message.getTimestamp()) + 10 * 24 * 60 * 60);
  }

  public static List<String> getComputedDistancesForUser(String userId) {
    List<String> result = new ArrayList<>();
    try (ResultSet rs = Controller.getDistancesForUser(userId)) {
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        result.add(rs.getString("distance_ciphertext"));
        Controller.addNewProcessedLocationPair(rs.getString("infected_location_id"), rs.getString("location_id"));
        rs.deleteRow();
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return result;
  }

  public static void computeNewDistances() {
    List<String> columnNames = Arrays.asList("loc.latitude_cos", "loc.latitude_sin", "loc.longitude_cos", "loc.longitude_sin",
        "infected_user_locations.latitude_cos", "infected_user_locations.latitude_sin",
        "infected_user_locations.longitude_cos", "infected_user_locations.longitude_sin",
        "loc.timestamp", "loc.user_id", "infected_user_locations.id", "loc.id");
    String sqlCommand = "SELECT *\n" +
        "from locations as loc\n" +
        "JOIN (SELECT * from locations where user_id IN (SELECT user_id from quarantined_users)) as infected_user_locations " +
        "on abs(infected_user_locations.timestamp - loc.timestamp) <= 5000 and infected_user_locations.user_id != loc.user_id\n" +
        "WHERE loc.id NOT IN (SELECT location_id from computed_distances where computed_distances.infected_location_id = infected_user_locations.id)\n" +
        "AND loc.id NOT IN(SELECT contact_loc_id from processed_distances where infected_loc_id = infected_user_locations.id)\n" +
        "LIMIT 100";

    try (ResultSet rs = Controller.getResultSetFromStatement(sqlCommand)) {
      if (rs == null) {
        return;
      }
      while (rs.next()) {
        Map<String, String> valueForColumn = new HashMap<>();
        for (String columnName : columnNames) {
          valueForColumn.put(columnName, rs.getString(columnName));
        }
        getDistanceAndUpdateDatabase(valueForColumn);
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  private static void getDistanceAndUpdateDatabase(Map<String, String> valueForColumn) {
    String distanceCiphertext = new String(jniBridge.getDistance(new char[][]{
        valueForColumn.get("loc.latitude_cos").toCharArray(),
        valueForColumn.get("loc.latitude_sin").toCharArray(),
        valueForColumn.get("loc.longitude_cos").toCharArray(),
        valueForColumn.get("loc.longitude_sin").toCharArray()
    }, new char[][]{
        valueForColumn.get("infected_user_locations.latitude_cos").toCharArray(),
        valueForColumn.get("infected_user_locations.latitude_sin").toCharArray(),
        valueForColumn.get("infected_user_locations.longitude_cos").toCharArray(),
        valueForColumn.get("infected_user_locations.longitude_sin").toCharArray()
    }));

    Controller.addNewContactDistance(distanceCiphertext, valueForColumn.get("loc.user_id"), valueForColumn.get("infected_user_locations.id"),
        valueForColumn.get("loc.id"), valueForColumn.get("loc.timestamp"));

  }
}
