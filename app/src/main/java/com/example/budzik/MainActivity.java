package com.example.budzik;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.budzik.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private ActivityMainBinding binding;

    private AppBarConfiguration appBarConfiguration;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private DrawerLayout mMainLayout;
    private boolean isLightSensorEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mMainLayout = findViewById(R.id.drawer_layout);

        setSupportActionBar(binding.toolbar);

        if (isFingerprintAvailable()) {
            showBiometricPrompt();
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        initializeLightSensor();
    }

    private void initializeLightSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float lightLevel = event.values[0];

                    if (lightLevel < 10) {
                        setAppTheme(1);
                    } else {
                        setAppTheme(0);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Nieprzydatne, ale pozostawiam dla kompletności
                }
            }, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Brak sensora światła", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isLightSensorRegistered = false;

    public void enableLightSensor(boolean enable) {
        isLightSensorEnabled = enable;
        if (enable && isLightSensorEnabled() && !isLightSensorRegistered) {
            initializeLightSensor();
            isLightSensorRegistered = true;
        } else if (!enable && isLightSensorRegistered) {
            unregisterLightSensor();
            isLightSensorRegistered = false;
        }
    }

    private void unregisterLightSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener((SensorEventListener) null);
        }
    }

    public boolean isLightSensorEnabled() {
        return isLightSensorEnabled;
    }

    private void setAppTheme(int backgroundIndex) {
        if (backgroundIndex == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private boolean isFingerprintAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);

        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        boolean isAvailable = false;

        switch (result) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                isAvailable = true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "Czytnik linii papilarnych niedostępny.", Toast.LENGTH_SHORT).show();
                break;
        }

        return isAvailable;
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("WakeUpCall")
                .setDescription("Użyj odcisku palca aby się zalogować")
                .setDeviceCredentialAllowed(true)
                .setConfirmationRequired(true)
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Obsługa błędów uwierzytelniania
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        // Uwierzytelnienie zakończone sukcesem - zapisz dane do bazy danych
                        mMainLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Obsługa nieudanego uwierzytelnienia
                    }
                });

        biometricPrompt.authenticate(promptInfo);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Przechodzenie do drugiego fragmentu
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_FirstFragment_to_SecondFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
