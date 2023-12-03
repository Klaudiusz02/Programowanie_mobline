package com.example.budzik;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.budzik.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment implements SensorEventListener {

    private FragmentSecondBinding binding;
    private String selectedMp3Uri;
    private SharedPreferences preferences;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int selectedBackground = getSelectedBackground();
        if (selectedBackground == 2) {
            initializeLightSensor();
        } else {
            setAppTheme(selectedBackground);
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        androidx.appcompat.app.ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        Button buttonChooseBackground = view.findViewById(R.id.buttonChooseBackground);
        buttonChooseBackground.setOnClickListener(v -> showBackgroundOptionsDialog());

        Button buttonAddCustomAlarm = view.findViewById(R.id.buttonAddCustomAlarm);
        buttonAddCustomAlarm.setOnClickListener(v -> openFilePickerForMp3());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        binding = null;
    }

    private void showBackgroundOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wybierz motyw aplikacji")
                .setSingleChoiceItems(new CharSequence[]{"Jasny", "Ciemny", "Tryb sensora"}, getSelectedBackground(),
                        (dialog, which) -> {
                            saveSelectedBackground(which);
                            if (which == 2) {
                                initializeLightSensor();
                            } else {
                                setAppTheme(which);
                            }
                            dialog.dismiss();
                        })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private int getSelectedBackground() {
        return preferences.getInt("selectedBackground", 0);
    }

    private void saveSelectedBackground(int backgroundIndex) {
        preferences.edit().putInt("selectedBackground", backgroundIndex).apply();
    }

    private void setAppTheme(int backgroundIndex) {
        if (backgroundIndex == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private void initializeLightSensor() {
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(requireContext(), "Brak sensora światła", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];

            if (lightLevel < 10) {
                setAppTheme(1);
            } else {
                setAppTheme(0);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nie przydatne ale jak wywalę to jest error więc zostawię :)
    }

    private void openFilePickerForMp3() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        String[] mimeTypes = {"audio/mpeg", "audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedMp3Uri = uri.toString();
                Toast.makeText(requireContext(), "Wybrano plik MP3: " + uri.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}