package com.example.budzik;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
                        // Pobierz zaznaczone dni tygodnia
                        String selectedDays = showDaysOfWeekDialog();

                        // Dodaj alarm z wybraną godziną i dniami tygodnia
                        addAlarm(selectedHour, selectedMinute, selectedDays);
                    }
                },
                hour, minute, true);

        timePickerDialog.show();
    }

    private String showDaysOfWeekDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wybierz dni tygodnia");

        // Utwórz kontener na CheckBox-y
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        // Dodaj CheckBox-y do układu
        for (String day : daysOfWeek) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(day);
            checkBox.setTextSize(18);
            checkBox.setPadding(0, 8, 0, 8);
            layout.addView(checkBox);
        }

        builder.setView(layout);

        final StringBuilder selectedDays = new StringBuilder();

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Zapisz zaznaczone dni tygodnia
                selectedDays.setLength(0); // Wyczyść istniejącą zawartość

                for (int i = 0; i < layout.getChildCount(); i++) {
                    CheckBox checkBox = (CheckBox) layout.getChildAt(i);
                    if (checkBox.isChecked()) {
                        selectedDays.append(checkBox.getText()).append(",");
                    }
                }

                if (selectedDays.length() > 0) {
                    // Usuń ostatni przecinek
                    selectedDays.deleteCharAt(selectedDays.length() - 1);
                }
            }
        });

        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        // Ustaw niestandardowy rozmiar okna dialogowego
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(600,height);
            }
        });

        dialog.show();

        return selectedDays.toString();
    }


    private void addAlarm(int hour, int minute, String selectedDays) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Sprawdź, czy ustawiona godzina jest wcześniejsza niż aktualna
        if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
            // Jeśli tak, to ustaw na następny dzień
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        String formattedTime = String.format("%02d:%02d", hour, minute);
        String alarmState = "Włączony";

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(formattedTime, alarmState + "|" + selectedDays);
        editor.apply();

        createAlarmCard(formattedTime, alarmState, selectedDays);

        setAlarm(calendar.getTimeInMillis(), formattedTime);
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

        for (Map.Entry<String, ?> entry : alarmsMap.entrySet()) {
            String[] values = entry.getValue().toString().split("\\|");
            String time = entry.getKey();
            String state = values[0];
            String selectedDays = values.length > 1 ? values[1] : "";
            createAlarmCard(time, state, selectedDays);
        }
    }

    private void createAlarmCard(String time, String state, String selectedDays) {
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
                editor.putString(time, isChecked ? "Włączony|" + selectedDays : "Wyłączony|" + selectedDays);
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

        // Dodawanie checkboxów dni tygodnia
        LinearLayout checkBoxLayout = new LinearLayout(requireContext());
        checkBoxLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        checkBoxLayout.setOrientation(LinearLayout.HORIZONTAL);

        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : daysOfWeek) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            checkBox.setText(day);
            checkBox.setChecked(selectedDays.contains(day));
            checkBoxLayout.addView(checkBox);
        }

        // Dodawanie widoków do układu wewnętrznego
        innerLayout.addView(textView);
        innerLayout.addView(switchButton);
        innerLayout.addView(deleteButton);
        innerLayout.addView(checkBoxLayout);

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