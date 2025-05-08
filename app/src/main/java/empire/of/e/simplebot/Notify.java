package empire.of.e.simplebot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class Notify extends Service {

	private static final int NOTIFY_ID = 6272;
	private static final String CHANNEL_ID = "zoopy";
	private NotificationManager notificationManager;
	private NotificationCompat.Builder builder;

	@Override
	public void onCreate() {
		super.onCreate();
		createNotificationChannel();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String title = getString(R.string.app_name);
		String descr = intent != null && intent.hasExtra("descr") ? intent.getStringExtra("descr") : getString(R.string.app_name);

		Intent targetIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(
				this,
				0,
				targetIntent,
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
						? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
						: PendingIntent.FLAG_UPDATE_CURRENT
		);

		builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(false)
				.setOngoing(true)
				.setColorized(true)
				.setContentTitle(title)
				.setContentText(descr)
				.setContentIntent(contentIntent)
				.setTicker(title)
				.setPriority(NotificationCompat.PRIORITY_LOW);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFY_ID, builder.build());

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (notificationManager != null) {
			notificationManager.cancel(NOTIFY_ID);
		}
		super.onDestroy();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		stopSelf();
		super.onTaskRemoved(rootIntent);
	}

	// These are optional overrides for completeness
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "Video Playback";
			String description = "Background Video Playback";
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			NotificationManager manager = getSystemService(NotificationManager.class);
			if (manager != null) {
				manager.createNotificationChannel(channel);
			}
		}
	}

	// Call this static method from MainActivity or elsewhere to update the notification
	public static void updateNotification(Context context, String description) {
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder updateBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(false)
				.setOngoing(true)
				.setColorized(true)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(description)
				.setTicker(context.getString(R.string.app_name))
				.setPriority(NotificationCompat.PRIORITY_LOW);

		Intent targetIntent = new Intent(context, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(
				context,
				0,
				targetIntent,
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
						? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
						: PendingIntent.FLAG_UPDATE_CURRENT
		);
		updateBuilder.setContentIntent(contentIntent);

		if (manager != null) {
			manager.notify(NOTIFY_ID, updateBuilder.build());
		}
	}
}
