package com.clericj.pipboywatchface;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.RemoteViews;


public class WatchFacePipBoyProvider extends AppWidgetProvider {

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		updateWidgets(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		String action = intent.getAction();
		if (Intent.ACTION_TIME_TICK.equals(action)
				|| Intent.ACTION_TIME_CHANGED.equals(action)) {
			updateWidgets(context);

		} else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			updateBatteryLevel(context, intent);
		}
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		registerReceivers(context);

		updateWidgets(context);
		Intent serviceIntent = new Intent(context, PipBoyClockService.class);
		context.startService(serviceIntent);
	}

	private void registerReceivers(Context context) {
		/* Регистрация отслеживаемых событий
		 */
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
	
		context.getApplicationContext().registerReceiver(this, filter);
	}

	private void updateBatteryLevel(Context context, Intent intent) {
		/* Обновление показаний батареи
		 */
    	int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    	int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int level = -1;

    	if (rawLevel >= 0 && scale > 0) {
            level = (rawLevel * 100) / scale;
        }
    	String batteryLevel = String.format("HP:%s%%", String.valueOf(level));	

    	AppWidgetManager manager = AppWidgetManager.getInstance(context);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.pipboy_widget);

		views.setTextViewText(R.id.battery, batteryLevel);
		manager.updateAppWidget(new ComponentName(context,
				WatchFacePipBoyProvider.class), views);
		
		System.out.println(String.format("battery: %s", batteryLevel));
	}
	
	@SuppressLint("SimpleDateFormat")
	private void updateWidgets(Context context) {
		/* Обновление данных в виджетах
		 */
		String timeOfDay = "";
		String timeFormat = "HH:mm";
		Date date = new Date();

		if(!is24HourFormat(context)) {
			timeOfDay = new SimpleDateFormat("a").format(date);
			timeFormat = "hh:mm";
		}
		String textDate = SimpleDateFormat.getDateInstance().format(date);
		String textTime = new SimpleDateFormat(timeFormat).format(date);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.pipboy_widget);

		views.setTextViewText(R.id.date, textDate);
		views.setTextViewText(R.id.time, textTime);
		views.setTextViewText(R.id.time_of_day, timeOfDay);
		manager.updateAppWidget(new ComponentName(context,
				WatchFacePipBoyProvider.class), views);

		System.out.println(String.format("date: %s", textDate));
		System.out.println(String.format("time: %s", textTime));
	}

	public static boolean is24HourFormat(Context context) {
		/* Проверка является ли текущий формат времени 24-часовым
		 */
		ContentResolver cv = context.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);

		return (strTimeFormat != null && strTimeFormat.equals("24")) ? true : false;
	}
}
