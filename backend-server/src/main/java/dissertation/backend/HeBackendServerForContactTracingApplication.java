package dissertation.backend;

import dissertation.backend.database.Cleaner;
import dissertation.backend.database.ContactTracingHelper;
import dissertation.backend.notification.FCMNotificationManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class HeBackendServerForContactTracingApplication {

  public static void main(String[] args) {
    startSchedulerForOldData();
    startSchedulerForComputingNewDistances();
    startSchedulerForNotification();
    SpringApplication.run(HeBackendServerForContactTracingApplication.class, args);
  }

  private static void startSchedulerForOldData() {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(Cleaner::deleteOldData, 0, 12, TimeUnit.HOURS);
  }

  private static void startSchedulerForComputingNewDistances() {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(ContactTracingHelper::computeNewDistances, 0, 15, TimeUnit.MINUTES);
  }

  private static void startSchedulerForNotification() {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(FCMNotificationManager::sendNotifications, 1, 30, TimeUnit.MINUTES);
  }
}
