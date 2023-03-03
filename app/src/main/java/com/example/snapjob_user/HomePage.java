package com.example.snapjob_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.snapjob_user.Fragment.BrowseFragment;
import com.example.snapjob_user.Fragment.FavoritesFragment;
import com.example.snapjob_user.Fragment.HomeFragment;
import com.example.snapjob_user.Fragment.LocationFragment;
import com.example.snapjob_user.Fragment.ProfileFragment;
import com.example.snapjob_user.Fragment.TransactionFragment;
import com.example.snapjob_user.Fragment.ViewWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navlistener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navlistener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.ic_transaction:
                    selectedFragment = new TransactionFragment();
                    break;
                case R.id.ic_browse:
                    selectedFragment = new BrowseFragment();
                    break;
                case R.id.ic_profile:
                    selectedFragment = new ProfileFragment();
                    break;
                default:
                    selectedFragment = new HomeFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
    };
}
