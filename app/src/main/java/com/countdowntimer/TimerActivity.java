package com.countdowntimer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import com.countdowntimer.databinding.ActivityMainBinding;
import com.countdowntimer.databinding.ActivityTimerBinding;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.util.Locale;

public class TimerActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSelectedListener, OnTimerFinishedListener {

    private static final String TAG = "TimerActivity";
    private ActivityTimerBinding binding;
    private long duration;
    private Intent intent;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            timerService = ((CountDownTimerService.CountDownTimerBinder) service)
                    .getInstance();

            timerService.getDurationLiveData().observe(TimerActivity.this, remainingTime -> {
                if (remainingTime != null)
                    binding.timerTv.setText(formateTimer(remainingTime));
                else
                    binding.timerTv.setText("00:00");
            });

            timerService.setFinishListener(TimerActivity.this);
            setVariables(timerService.getIsRunning());

            if (!timerService.getIsStarted()){
                showTimePickerDialog();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



    private CountDownTimerService timerService;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DataBindingUtil.setContentView(this,R.layout.activity_timer);
        intent = new Intent(getApplicationContext(), CountDownTimerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        setBtnsClick();
    }

    private void setBtnsClick() {
        binding.startBtn.setOnClickListener(v -> {
            if (timerService.getIsStarted() && !timerService.getIsRunning()) {
                Log.d(TAG, "setBtnsClick: "+1);
                timerService.resumeTimer();
                setVariables(true);
                return;
            }else if (timerService.getIsRunning()){
                Log.d(TAG, "setBtnsClick: "+2);
                return;
            }
            if (duration<=0){
                Toast.makeText(getApplicationContext(),"Please select a duration!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "setBtnsClick: ");
            setVariables(true);
            intent.putExtra("duration", duration);
            startService(intent);
        });

//        will open time selector
        binding.timerIcon.setOnClickListener(v->{
            if (!timerService.getIsStarted()) {
                showTimePickerDialog();
            }
        });

        binding.pauseBtn.setOnClickListener(v->{
            timerService.pauseTimer();
            setVariables(false);
        });

        binding.stopBtn.setOnClickListener(v->{
            duration=timerService.getOriginalDuration();
            binding.timerTv.setText(formateTimer(duration));
            timerService.resetTimer();
            setVariables(false);
        });

        binding.restartBtn.setOnClickListener(v->{
            setVariables(false);
            binding.setIsFinished(false);
            duration=timerService.getOriginalDuration();
            binding.timerTv.setText(formateTimer(duration));
        });
    }


    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new
                TimePickerDialog(TimerActivity.this);
//        timePickerDialog.setStyle(DialogFragment.STYLE_NO_TITLE,R.style.Custom_BSD_Style);
        timePickerDialog.show(getSupportFragmentManager(), "time picker");
    }

    private void setVariables(boolean isRunning) {
        binding.setIsRunning(isRunning);
    }

    /**
     * formate timer shown in textview
     *
     * @param time
     * @return
     */
    private String formateTimer(long time) {
        String str = "00:00";
        /*int hour = 0;
        if(time>=1000*3600){
            hour = (int)(time/(1000*3600));
            time -= hour*1000*3600;
        }*/
        int minute = 0;
        if (time >= 1000 * 60) {
            minute = (int) (time / (1000 * 60));
            time -= minute * 1000 * 60;
        }
        int sec = (int) (time / 1000);
//        str = formateNumber(hour)+":"+formateNumber(minute)+":"+formateNumber(sec);
        str = formateNumber(minute) + ":" + formateNumber(sec);
        return str;
    }

    /**
     * formate time number with two numbers auto add 0
     *
     * @param time
     * @return
     */
    private String formateNumber(int time) {
        return String.format(Locale.getDefault(), "%02d", time);
    }

    @Override
    public void onTimeSelected(long time) {
        this.duration = time;
        binding.timerTv.setText(formateTimer(time));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (intent != null) {
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onTimerFinished() {
        binding.setIsRunning(false);
        binding.setIsFinished(true);
    }
}