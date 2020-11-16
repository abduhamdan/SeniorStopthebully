package com.example.tabview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FrameLayout frameLayout;


    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragmentholderactivity);

        bottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        frameLayout = (FrameLayout) findViewById(R.id.main_frame);
        fAuth=FirebaseAuth.getInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new HomeFragment()).commit();

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
              Fragment selectedFrag=null;
                switch (item.getItemId()){

                    case R.id.nav_home:

                        selectedFrag=new HomeFragment();
                       break;

                    case R.id.nav_chat:

                        selectedFrag=new ChatFragment();
                        break;

                    case R.id.nav_resources:

                        selectedFrag=new ResourcesFragment();
                        break;

                    case R.id.nav_profile:

                        selectedFrag=new ProfileFragment();
                        break;

                }

                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, selectedFrag).commit();
                return true;
            }
        });

    }

    protected void onStart() {


        super.onStart();

        if (fAuth.getCurrentUser()==null) //if the user isnt logged in it'll go back to the login page.
        {
            finish();
            startActivity(new Intent(this, LoginPage.class));
        }
    }


}