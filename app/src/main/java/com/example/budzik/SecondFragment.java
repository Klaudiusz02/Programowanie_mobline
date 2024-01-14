package com.example.budzik;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.budzik.databinding.FragmentSecondBinding;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private SharedPreferences preferences;

    private Executor executor = Executors.newSingleThreadExecutor();

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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFingerprintAvailable()) {
                    showBiometricPrompt();
                } else {
                    Toast.makeText(requireContext(), "Czytnik linii papilarnych niedostępny.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isFingerprintAvailable() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());

        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        boolean isAvailable = false;

        switch (result) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                isAvailable = true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(requireContext(), "Czytnik linii papilarnych niedostępny.", Toast.LENGTH_SHORT).show();
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

        BiometricPrompt biometricPrompt = new BiometricPrompt(requireActivity(), executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                    }
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                                navController.navigate(R.id.action_SecondFragment_to_FirstFragment);
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Ukryj element menu o ID action_settings w SecondFragment
        MenuItem logoutMenuItem = menu.findItem(R.id.action_settings);
        if (logoutMenuItem != null) {
            logoutMenuItem.setVisible(false);
        }
    }
}
