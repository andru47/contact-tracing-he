package dissertation.backend.database;

import dissertation.backend.CiphertextWrapper;
import dissertation.backend.JNIBridge;
import dissertation.backend.config.Config;
import dissertation.backend.config.EncryptionType;
import dissertation.backend.notification.FCMNotificationManager;
import dissertation.backend.serialization.ComputedDistanceMessage;
import dissertation.backend.serialization.NewCaseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContactTracingHelper {
  private final static JNIBridge jniBridge = new JNIBridge();
  private final static Logger logger = LogManager.getLogger(ContactTracingHelper.class);

  public static void addNewCovidCase(NewCaseMessage message) {
    Controller.addNewInfectedUser(message.getUserId(), Long.parseLong(message.getTimestamp()) + 10 * 24 * 60 * 60);
  }

  public static List<ComputedDistanceMessage> getComputedDistancesForUser(String userId, boolean partial) {
    List<ComputedDistanceMessage> result = new ArrayList<>();
    try (ResultSet rs = Controller.getDistancesForUser(userId, partial)) {
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        ComputedDistanceMessage message;
        if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK && !partial) {
          message = new ComputedDistanceMessage(rs.getString("partial_distance"), rs.getString("partial_altitude_difference"), rs.getString("distance_ciphertext1"), rs.getString("altitude_difference1"), rs.getString("infected_user_id"), Long.parseLong(rs.getString("timestamp")),
              Long.parseLong(rs.getString("timestamp_end")));
        } else if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK) {
          message = new ComputedDistanceMessage(rs.getString("distance_ciphertext2"), rs.getString("altitude_difference2"), rs.getString("row_id"), rs.getString("infected_user_id"), Long.parseLong(rs.getString("timestamp")),
              Long.parseLong(rs.getString("timestamp_end")));
        } else {
          message = new ComputedDistanceMessage(rs.getString("distance_ciphertext1"), rs.getString("altitude_difference1"), rs.getString("row_id"), rs.getString("infected_user_id"), Long.parseLong(rs.getString("timestamp")),
              Long.parseLong(rs.getString("timestamp_end")));
        }
        result.add(message);
        if (!partial) {
          Controller.addNewProcessedLocationPair(rs.getString("infected_location_id"), rs.getString("location_id"));
          rs.deleteRow();
        } else {
          rs.updateBoolean("downloaded", true);
          rs.updateRow();
        }
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return result;
  }

  public static void checkContacts() {
    String getPairsOfContactsStatement = "SELECT unique user_id, infected_user_id from contacts where user_id not in (Select user_id from quarantined_users)";
    List<List<String>> pairsOfContacts = new ArrayList<>();
    try (ResultSet rs = Controller.getResultSetFromStatement(getPairsOfContactsStatement)) {
      while (rs.next()) {
        pairsOfContacts.add(List.of(rs.getString("user_id"), rs.getString("infected_user_id")));
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    logger.debug("Found {} pairs of users", pairsOfContacts.size());

    HashMap<String, Long> positives = new HashMap<>();
    ExecutorService service = Executors.newFixedThreadPool(5);
    List<Future<List<Object>>> futures = new ArrayList<>();
    for (List<String> pairOfContacts : pairsOfContacts) {
      futures.add(service.submit(() -> {
        Long contactTime = getContact(pairOfContacts);
        if (contactTime != 0L) {
          return List.of(pairOfContacts.get(0), contactTime);
        }
        return null;
      }));
    }

    for (Future<List<Object>> currentFuture : futures) {
      List<Object> currentPositive = null;
      try {
        currentPositive = currentFuture.get();
      } catch (ExecutionException | InterruptedException e) {
        e.printStackTrace();
      }
      if (currentPositive == null || (positives.containsKey((String) currentPositive.get(0)) && positives.get((String) currentPositive.get(0)) <= (Long) currentPositive.get(1))) {
        continue;
      }
      String userId = (String) currentPositive.get(0);
      Long timestamp = (Long) currentPositive.get(1);
      positives.put(userId, timestamp);
    }

    for (String positiveId : positives.keySet()) {
      FCMNotificationManager.sendContactNotification(positiveId, String.valueOf(positives.get(positiveId) + 10 * 24 * 60 * 60));
      Controller.addNewInfectedUser(positiveId, positives.get(positiveId) + 10 * 24 * 60 * 60);
    }
  }

  private static Long getContact(List<String> pairContacts) {
    String userId = pairContacts.get(0);
    String infectedUserId = pairContacts.get(1);
    List<List<Long>> timestamps = new ArrayList<>();

    try (ResultSet rs = Controller.getContacts(userId, infectedUserId)) {
      if (rs == null) {
        return 0L;
      }

      while (rs.next()) {
        timestamps.add(List.of(rs.getLong("timestamp"), rs.getLong("timestamp_end")));
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    if (timestamps.size() == 0) {
      return 0L;
    }

    logger.debug("Found {} contacts between {} and {}", timestamps.size(), pairContacts.get(0), pairContacts.get(1));

    int startIndex = 0, currentIndex = 0;
    long currentDuration = 0;
    while (currentIndex < timestamps.size()) {
      while (startIndex < currentIndex && timestamps.get(currentIndex).get(0) - timestamps.get(startIndex).get(0) >= 24 * 60 * 60) {
        currentDuration -= (timestamps.get(startIndex).get(1) - timestamps.get(startIndex).get(0));
        startIndex++;
      }
      currentDuration += (timestamps.get(currentIndex).get(1) - timestamps.get(currentIndex).get(0));
      if (currentDuration >= 15 * 60) {
        return timestamps.get(startIndex).get(0);
      }
      ++currentIndex;
    }

    return 0L;
  }

  public static void computeNewDistances() {
    List<String> columnNames = Arrays.asList("loc.latitude_cos", "loc.latitude_sin", "loc.longitude_cos", "loc.longitude_sin",
        "infected_user_locations.latitude_cos", "infected_user_locations.latitude_sin",
        "infected_user_locations.longitude_cos", "infected_user_locations.longitude_sin",
        "loc.timestamp", "loc.user_id", "infected_user_locations.id", "loc.id", "infected_user_locations.user_id",
        "loc.timestamp_end", "infected_user_locations.timestamp", "infected_user_locations.timestamp_end",
        "loc.altitude", "infected_user_locations.altitude");
    String sqlCommand = "SELECT *\n" +
        "from locations as loc\n" +
        "JOIN (SELECT * from locations where user_id IN (SELECT user_id from quarantined_users)) as infected_user_locations " +
        "on ((infected_user_locations.timestamp >= loc.timestamp and infected_user_locations.timestamp <= loc.timestamp_end) or\n" +
        "(loc.timestamp >= infected_user_locations.timestamp and loc.timestamp <= infected_user_locations.timestamp_end))\n" +
        "and infected_user_locations.user_id != loc.user_id\n" +
        "WHERE loc.id NOT IN (SELECT location_id from computed_distances where computed_distances.infected_location_id = infected_user_locations.id)\n" +
        "AND loc.id NOT IN(SELECT contact_loc_id from processed_distances where infected_loc_id = infected_user_locations.id)\n" +
        "AND loc.user_id NOT IN (SELECT user_id from quarantined_users)\n" +
        "LIMIT 100";

    try (ResultSet rs = Controller.getResultSetFromStatement(sqlCommand)) {
      if (rs == null) {
        return;
      }
      Map<String, List<String>> keysMap = Config.getEncryptionType() == EncryptionType.LATTIGO_MK ? new HashMap<>() : null;
      while (rs.next()) {
        Map<String, String> valueForColumn = new HashMap<>();
        for (String columnName : columnNames) {
          valueForColumn.put(columnName, rs.getString(columnName));
        }

        if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK) {
          updateKeys(valueForColumn.get("loc.user_id"), keysMap);
          updateKeys(valueForColumn.get("infected_user_locations.user_id"), keysMap);
        }

        getDistanceAndUpdateDatabase(valueForColumn, keysMap);
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  private static void updateKeys(String userId, Map<String, List<String>> keysMap) {
    if (keysMap.containsKey(userId)) {
      return;
    }
    keysMap.put(userId, Controller.getKeysForUser(userId));
  }

  private static void getDistanceAndUpdateDatabase(Map<String, String> valueForColumn, Map<String, List<String>> keysMap) {
    String userId = valueForColumn.get("loc.user_id");
    String infectedUserId = valueForColumn.get("infected_user_locations.user_id");
    CiphertextWrapper distanceCiphertext, altitudeDifferenceCiphertext;

    if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK) {
      distanceCiphertext = jniBridge.getMultiKeyDistance(new char[][]{
              valueForColumn.get("loc.latitude_cos").toCharArray(),
              valueForColumn.get("loc.latitude_sin").toCharArray(),
              valueForColumn.get("loc.longitude_cos").toCharArray(),
              valueForColumn.get("loc.longitude_sin").toCharArray()
          }, new char[][]{
              valueForColumn.get("infected_user_locations.latitude_cos").toCharArray(),
              valueForColumn.get("infected_user_locations.latitude_sin").toCharArray(),
              valueForColumn.get("infected_user_locations.longitude_cos").toCharArray(),
              valueForColumn.get("infected_user_locations.longitude_sin").toCharArray()
          }, keysMap.get(userId).get(0).toCharArray(), keysMap.get(userId).get(1).toCharArray(),
          keysMap.get(infectedUserId).get(0).toCharArray(), keysMap.get(infectedUserId).get(1).toCharArray());
      altitudeDifferenceCiphertext = jniBridge.getMultiKeyAltitudeDifference(
          valueForColumn.get("loc.altitude").toCharArray(),
          valueForColumn.get("infected_user_locations.altitude").toCharArray(),
          keysMap.get(userId).get(0).toCharArray(), keysMap.get(userId).get(1).toCharArray(),
          keysMap.get(infectedUserId).get(0).toCharArray(), keysMap.get(infectedUserId).get(1).toCharArray());
    } else {
      distanceCiphertext = new CiphertextWrapper();
      altitudeDifferenceCiphertext = new CiphertextWrapper();
      distanceCiphertext.setComputedCiphertext1(jniBridge.getDistance(new char[][]{
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
      altitudeDifferenceCiphertext.setComputedCiphertext1(jniBridge.getAltitudeDifference(
          valueForColumn.get("loc.altitude").toCharArray(),
          valueForColumn.get("infected_user_locations.altitude").toCharArray()));
    }

    Controller.addNewContactDistance(distanceCiphertext, altitudeDifferenceCiphertext, userId, valueForColumn.get("infected_user_locations.id"),
        valueForColumn.get("loc.id"), infectedUserId,
        getStart(valueForColumn.get("loc.timestamp"), valueForColumn.get("infected_user_locations.timestamp")),
        getStop(valueForColumn.get("loc.timestamp_end"), valueForColumn.get("infected_user_locations.timestamp_end")));
  }

  private static String getStart(String first, String second) {
    int firstNumber = Integer.parseInt(first);
    int secondNumber = Integer.parseInt(second);
    return String.valueOf(Math.max(firstNumber, secondNumber));
  }

  private static String getStop(String first, String second) {
    int firstNumber = Integer.parseInt(first);
    int secondNumber = Integer.parseInt(second);
    return String.valueOf(Math.min(firstNumber, secondNumber));
  }
}
