package com.example.budzik;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.TimePickerDialog;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.example.budzik.databinding.FragmentFirstBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Map;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    private SharedPreferences sharedPreferences;
    private LinearLayout containerLayout;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        containerLayout = view.findViewById(R.id.container);

        sharedPreferences = requireContext().getSharedPreferences("Alarms", Context.MODE_PRIVATE);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }

            private void showTimePickerDialog() {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        requireContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                addAlarm(selectedHour, selectedMinute);
                            }
                        },
                        hour, minute, true);

                timePickerDialog.show();
            }
        });

        restoreAlarms();

        return view;
    }

    private void addAlarm(int hour, int minute) {
        String formattedTime = String.format("%02d:%02d", hour, minute);
        String alarmState = "Włączony";

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(formattedTime, alarmState);
        editor.apply();

        createAlarmCard(formattedTime, alarmState);
    }

    private void restoreAlarms() {
        Map<String, ?> alarmsMap = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : alarmsMap.entrySet()) {
            String time = entry.getKey();
            String state = entry.getValue().toString();
            createAlarmCard(time, state);
        }
    }

    private void createAlarmCard(String time, String state) {
        CardView cardView = new CardView(requireContext());
        int heightInPixels = 300;
        int leftMargin = 16;
        int topMargin = 8;
        int rightMargin = 16;
        int bottomMargin = 8;
        float textSizeInSp = 30;

        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightInPixels
        );

        cardLayoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setCardElevation(8);
        cardView.setRadius(16);

        LinearLayout innerLayout = new LinearLayout(requireContext());
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(16, 16, 16, 16);

        TextView textView = new TextView(requireContext());
        textView.setText(time);
        textView.setTextSize(18);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);


        Switch switchButton = new Switch(requireContext());
        switchButton.setChecked(state.equals("Włączony"));

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(time, isChecked ? "Włączony" : "Wyłączony");
                editor.apply();
            }
        });

        switchButton.setThumbResource(R.drawable.custom_thumb);
        switchButton.setTrackResource(R.drawable.custom_track);

        Button deleteButton = new Button(requireContext());
        deleteButton.setText("Usuń");
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                containerLayout.removeView(cardView);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(time);
                editor.apply();
            }
        });

        innerLayout.addView(textView);
        innerLayout.addView(switchButton);
        innerLayout.addView(deleteButton);

        cardView.addView(innerLayout);

        containerLayout.addView(cardView);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}