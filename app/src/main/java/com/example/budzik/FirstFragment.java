package com.example.budzik;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.TimePickerDialog;

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

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

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

                                Switch switchButton = new Switch(requireContext());
                                switchButton.setChecked(false);

// Tworzenie CardView
                                CardView cardView = new CardView(requireContext());
                                int heightInPixels = 200;
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
                                innerLayout.setOrientation(LinearLayout.HORIZONTAL);
                                innerLayout.setPadding(16, 16, 16, 16);

                                TextView textView = new TextView(requireContext());
                                textView.setText(selectedHour + ":" + selectedMinute);
                                textView.setTextSize(18);
                                textView.setPadding(16, 16, 16, 16);
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);

                                LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1.0f
                                );
                                textView.setLayoutParams(textLayoutParams);
                                switchButton.setLayoutParams(textLayoutParams);

                                innerLayout.addView(textView);
                                innerLayout.addView(switchButton);

                                cardView.addView(innerLayout);

                                LinearLayout container = requireView().findViewById(R.id.container);
                                container.addView(cardView);

                            }
                        },
                        hour, minute, true);

                timePickerDialog.show();
            }
        });
        return view;

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