package com.countdowntimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;

public class CountDownTimerService extends Service {

    private static final String TAG = "CountDownTimerService";
    private boolean isRunning;
    private boolean isStarted;
    private CountDownTimer countDownTimer;
    private long timeRemaining;
    private long originalDuration;
    private OnTimerFinishedListener timerFinishedListener;
    private MutableLiveData<Long> durationLiveData;

    public CountDownTimerService() {
    }

    public MutableLiveData<Long> getDurationLiveData() {
        return durationLiveData;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new CountDownTimerBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        durationLiveData = new MutableLiveData<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("action") && intent.getExtras().getString("action").equals("stop")) {
            resetTimer();
            return START_NOT_STICKY;
        }
//        Starting service as foreground service
        startFs();
        originalDuration = intent.getExtras().getLong("duration");

        countDownTimer = new CountDownTimer(originalDuration, 1000) {
            public void onTick(long millisUntilFinished) {
                performOnTick(millisUntilFinished);
            }

            public void onFinish() {
                performFinish();
            }
        }.start();

        return START_STICKY;
    }

    private void performFinish() {
        SoundPool soundPool;
        int templeBell;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        int loadId = soundPool.load(getApplicationContext(), R.raw.gong, 1);
        soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
            soundPool1.play(loadId,50,50,1,0,2);
            timerFinishedListener.onTimerFinished();
            resetTimer();
        });
    }

    private void performOnTick(long millisUntilFinished) {
        timeRemaining = millisUntilFinished;
        isStarted = true;
        isRunning = true;
        durationLiveData.postValue(millisUntilFinished);
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    public boolean getIsStarted() {
        return isStarted;
    }

    public void pauseTimer() {
        isRunning = false;
        countDownTimer.cancel();
        countDownTimer = null;
    }

    public void resetTimer() {
        isRunning = false;
        isStarted = false;
        countDownTimer.cancel();
        countDownTimer = null;
        stopForeground(true);
        stopSelf();
    }

    public long getOriginalDuration() {
        return originalDuration;
    }

    public void resumeTimer() {
        isRunning = true;
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                performOnTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                performFinish();
            }
        };
        countDownTimer.start();
    }

    private void startFs() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        Intent intent = new Intent(getApplicationContext(), CountDownTimerService.class);
        intent.putExtra("action", "stop");
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 123,
                intent, 0);
        if (SDK_INT >= O) {
            notificationManager.createNotificationChannel(new NotificationChannel("abc", "CountDown Timer",
                    NotificationManager.IMPORTANCE_DEFAULT));
            notification = new Notification.Builder(getApplicationContext(), "abc")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Count Down Timer Running")
                    .addAction(new Notification.Action.Builder(
                            Icon.createWithResource(getApplicationContext(),
                                    R.drawable.ic_round_stop_24), "Stop Timer", pendingIntent).build())
                    .build();
        } else {
            notification = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Count Down Timer Running")
                    .addAction(R.drawable.ic_round_stop_24, "Stop Timer", pendingIntent)
                    .build();
        }

        if (SDK_INT >= Q) {
            startForeground(424, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(424, notification);
        }
    }

    public void setFinishListener(OnTimerFinishedListener timerFinishedListener) {
        this.timerFinishedListener = timerFinishedListener;
    }

    public class CountDownTimerBinder extends Binder {
        CountDownTimerService getInstance() {
            return CountDownTimerService.this;
        }
    }

}