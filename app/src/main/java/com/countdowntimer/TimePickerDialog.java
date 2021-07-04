package com.countdowntimer;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.countdowntimer.databinding.DurationPickerDialogBinding;

public class TimePickerDialog extends DialogFragment {

    private static final String TAG = "TimePickerDialog";
    private final OnTimeSelectedListener onTimeSelectedListener;
    private DurationPickerDialogBinding binding;
    private long duration=-1;
    private boolean isSelected;

    public TimePickerDialog(OnTimeSelectedListener onTimeSelectedListener) {
        this.onTimeSelectedListener = onTimeSelectedListener;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.MyDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DurationPickerDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.durationsGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isSelected = true;
                if (checkedId != R.id.custom_radio) {
//                    setCustomEtvActivity(false);
                    String text = ((RadioButton) view.findViewById(checkedId))
                            .getText().toString();
                    String[] split = text.split(":");
                    int mints = Integer.parseInt(split[0]);
                    int secs = Integer.parseInt(split[1]);
                    setTime(mints, secs);
                } else {
//                    setCustomEtvActivity(true);
                    if (!binding.customDurationSec.getText().toString().equals("") &&
                            !binding.customDurationMint.getText().toString().equals(""))
                        setTime(Integer.parseInt(binding.customDurationMint.getText().toString()),
                                Integer.parseInt(binding.customDurationSec.getText().toString()));
                    else
                        duration=-1;
                }
            }
        });

        binding.timerDialogDone.setOnClickListener(v -> {
            if (isSelected && duration>0) {
                onTimeSelectedListener.onTimeSelected(duration);
                dismiss();
            }
        });

//        Edit texts setup

        binding.customDurationMint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!binding.customDurationSec.getText().toString().equals("") &&
                        !binding.customDurationMint.getText().toString().equals(""))
                    setTime(Integer.parseInt(binding.customDurationMint.getText().toString()),
                            Integer.parseInt(binding.customDurationSec.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.customDurationSec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!binding.customDurationSec.getText().toString().equals("") &&
                        !binding.customDurationMint.getText().toString().equals("")) {
                    setTime(Integer.parseInt(binding.customDurationMint.getText().toString()),
                            Integer.parseInt(binding.customDurationSec.getText().toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.customDurationSec.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                binding.durationsGrp.check(R.id.custom_radio);
        });

        binding.customDurationMint.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.durationsGrp.check(R.id.custom_radio);
            }
        });

    }

    private void setTime(int mints, int secs) {
        duration = mints * 60 * 1000;
        duration += secs * 1000;
    }

    private void setCustomEtvActivity(boolean isActive) {
        binding.customDurationMint.setEnabled(isActive);
        binding.customDurationMint.setFocusable(isActive);
        binding.customDurationMint.setClickable(isActive);
        binding.customDurationSec.setEnabled(isActive);
        binding.customDurationSec.setFocusable(isActive);
        binding.customDurationSec.setClickable(isActive);
    }

    public interface OnTimeSelectedListener {
        void onTimeSelected(long time);
    }

}
