package dissertation.backend.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

/***
 * Helper class that deletes all stale data (>= 14 days old) from the tables. It is called by the scheduler mechanism.
 */
public class Cleaner {
  private static final Logger logger = LogManager.getLogger(Cleaner.class);
  private static final String SQL_DELETE_STATEMENT_LOCATIONS = "DELETE from locations where\n" +
      "? - locations.timestamp >= 1209600";
  private static final String SQL_DELETE_STATEMENT_QUARANTINED = "DELETE from quarantined_users where\n" +
      "? - quarantined_users.end >= 1209600";

  public static void deleteOldData() {
    logger.info("I have received command to clean stale data");
    Controller.executeDeleteStaleStatement(SQL_DELETE_STATEMENT_LOCATIONS, Instant.now().getEpochSecond());
    Controller.executeDeleteStaleStatement(SQL_DELETE_STATEMENT_QUARANTINED, Instant.now().getEpochSecond());
    Controller.executeDeleteOldContacts();
    logger.info("I have finished cleaning stale data");
  }
}
