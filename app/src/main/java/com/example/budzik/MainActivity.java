package com.example.budzik;

import static android.app.PendingIntent.getActivity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_main);

        // Znajdź przycisk FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);

        // Ustaw listener na kliknięcie przycisku
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }

            private void showTimePickerDialog() {
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Tutaj możesz wykorzystać wybraną godzinę i minutę
                        String selectedTime = hourOfDay + ":" + minute;
                        // Możesz zrobić coś z wybranym czasem, np. wyświetlić go na ekranie
                        // W tym przypadku, możesz zaimplementować wyświetlenie wybranej godziny w polu TextView
                    }
                }, 12, 0, true); // Ustawiamy początkową godzinę i minutę

                timePickerDialog.show(); // Pokazujemy dialog
            }
        });*/



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);




        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_sleep) {
                    NavHostFragment.findNavController((Fragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main))
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                } else if (id == R.id.nav_alarm) {
                    NavHostFragment.findNavController((Fragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main))
                            .navigate(R.id.action_SecondFragment_to_FirstFragment);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
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
