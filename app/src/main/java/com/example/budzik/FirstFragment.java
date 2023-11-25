package com.example.budzik;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.TimePickerDialog;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FirstFragment extends Fragment {

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
        });

        restoreAlarms();

        return view;
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
                        // Dodaj alarm z wybraną godziną i dniami tygodnia
                        addAlarm(selectedHour, selectedMinute);
                    }
                },
                hour, minute, true);

        timePickerDialog.show();
    }


    private void addAlarm(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Sprawdź, czy ustawiona godzina jest wcześniejsza niż aktualna
        if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
            // Jeśli tak, to ustaw na następny dzień
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        String formattedTime = String.format("%02d:%02d", hour, minute);
        String alarmState = "Włączony";

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(formattedTime, alarmState);
        editor.apply();

        // Przeprowadź sortowanie alarmów po dodaniu nowego alarmu
        List<Map.Entry<String, ?>> sortedAlarms = sortAlarmsByTime(sharedPreferences.getAll());

        // Usuń wszystkie karty i utwórz je ponownie z posortowaną listą alarmów
        containerLayout.removeAllViews();
        for (Map.Entry<String, ?> entry : sortedAlarms) {
            String[] values = entry.getValue().toString().split("\\|");
            String time = entry.getKey();
            String state = values[0];
            createAlarmCard(time, state);
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        setAlarm(calendar.getTimeInMillis(), formattedTime);

        // Oblicz czas do uruchomienia alarmu i wyświetl toast
        calculateAndShowTimeToAlarm(calendar.getTimeInMillis());
    }

    private void calculateAndShowTimeToAlarm(long alarmTimeInMillis) {
        long currentTimeInMillis = System.currentTimeMillis();
        long timeDifference = alarmTimeInMillis - currentTimeInMillis;

        if (timeDifference > 0) {
            long secondsToAlarm = timeDifference / 1000;
            long minutesToAlarm = (secondsToAlarm + 59) / 60; // Zaokrąglanie w górę
            long hoursToAlarm = minutesToAlarm / 60;

            long remainingMinutes = minutesToAlarm % 60;

            // Jeśli czas do alarmu jest mniej niż godzina, wyświetl minuty
            if (hoursToAlarm == 0) {
                String toastMessage = String.format("Czas do alarmu: %d minut", remainingMinutes);
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_LONG).show();
            } else {
                // Wyświetl czas w godzinach i minutach
                String toastMessage = String.format("Czas do alarmu: %d godzin i %02d minut", hoursToAlarm, remainingMinutes);
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    // Metoda do sortowania alarmów według czasu
    private List<Map.Entry<String, ?>> sortAlarmsByTime(Map<String, ?> alarmsMap) {
        List<Map.Entry<String, ?>> sortedAlarms = new ArrayList<>(alarmsMap.entrySet());

        Collections.sort(sortedAlarms, new Comparator<Map.Entry<String, ?>>() {
            @Override
            public int compare(Map.Entry<String, ?> entry1, Map.Entry<String, ?> entry2) {
                return entry1.getKey().compareTo(entry2.getKey());
            }
        });

        return sortedAlarms;
    }


    private void setAlarm(long timeInMillis, String alarmTime) {
        // Utwórz intent do obsługi powiadomienia
        Intent alarmIntent = new Intent(requireContext(), AlarmReceiver.class);
        alarmIntent.putExtra("ALARM_TIME", alarmTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Ustaw alarm za pomocą AlarmManager
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private void restoreAlarms() {
        Map<String, ?> alarmsMap = sharedPreferences.getAll();

        // Create a list to store the alarms
        List<Map.Entry<String, ?>> sortedAlarms = new ArrayList<>(alarmsMap.entrySet());

        // Sort the alarms based on their keys (time)
        Collections.sort(sortedAlarms, new Comparator<Map.Entry<String, ?>>() {
            @Override
            public int compare(Map.Entry<String, ?> entry1, Map.Entry<String, ?> entry2) {
                return entry1.getKey().compareTo(entry2.getKey());
            }
        });

        for (Map.Entry<String, ?> entry : sortedAlarms) {
            String[] values = entry.getValue().toString().split("\\|");
            String time = entry.getKey();
            String state = values[0];
            String selectedDays = values.length > 1 ? values[1] : "";
            createAlarmCard(time, state);
        }
    }

    private void createAlarmCard(String time, String state) {
        // Tworzenie karty alarmu
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

        // Tworzenie układu wewnętrznego
        LinearLayout innerLayout = new LinearLayout(requireContext());
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(16, 16, 16, 16);

        // Dodawanie tekstu z czasem
        TextView textView = new TextView(requireContext());
        textView.setText(time);
        textView.setTextSize(18);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);

        // Dodawanie przełącznika
        Switch switchButton = new Switch(requireContext());
        switchButton.setChecked(state.equals("Włączony"));
        switchButton.setTag(time);

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

        // Dodawanie przycisku usuwania
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


        // Dodawanie widoków do układu wewnętrznego
        innerLayout.addView(textView);
        innerLayout.addView(switchButton);
        innerLayout.addView(deleteButton);

        // Dodawanie układu wewnętrznego do karty
        cardView.addView(innerLayout);

        // Dodawanie karty do głównego kontenera
        containerLayout.addView(cardView);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //binding = null;
    }

}