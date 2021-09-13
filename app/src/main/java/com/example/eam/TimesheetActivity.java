package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.eam.databinding.ActivityTimesheetBinding;
import com.example.eam.menu.AdminFragment;
import com.example.eam.menu.AssetsFragment;
import com.example.eam.menu.ChatsFragment;
import com.example.eam.menu.ProfileFragment;
import com.example.eam.menu.TimesheetsFragment;
import com.example.eam.menu.TodayFragment;
import com.google.android.material.navigation.NavigationBarView;

public class TimesheetActivity extends AppCompatActivity {
    public static final String TAG = "TimesheetActivity";
    private ActivityTimesheetBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timesheet);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TodayFragment()).commit();

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_today:
                        selectedFragment = new TodayFragment();
                        break;
                    case R.id.nav_timesheet:
                        selectedFragment = new TimesheetsFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });
    }
}