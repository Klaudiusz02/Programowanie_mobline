package com.example.budzik;

import static android.app.PendingIntent.getActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.budzik.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.WindowCompat;
import com.example.budzik.R;
import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean isLightSensorEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_sleep) {
                NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main))
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            } else if (id == R.id.nav_alarm) {
                NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main))
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Inicjalizacja sensora światła
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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