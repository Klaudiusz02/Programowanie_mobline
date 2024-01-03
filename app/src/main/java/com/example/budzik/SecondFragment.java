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

        // Usuń kod związany z ustawianiem motywu
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        androidx.appcompat.app.ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        Button buttonAddCustomAlarm = view.findViewById(R.id.buttonAddCustomAlarm);
        buttonAddCustomAlarm.setOnClickListener(v -> openFilePickerForMp3());
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeLightSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterLightSensor();
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

    private void unregisterLightSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];

            if (lightLevel < 10) {
                // Tutaj możesz ewentualnie dodatkowo zareagować na zmianę światła,
                // np. zmienić kolor tła lub wykonać inne działania
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nieprzydatne, ale zostawiam dla kompletności
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
                String selectedMp3Uri = uri.toString();
                Toast.makeText(requireContext(), "Wybrano plik MP3: " + uri.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
