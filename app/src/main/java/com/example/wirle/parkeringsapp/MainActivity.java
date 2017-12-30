package com.example.wirle.parkeringsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PlaceFragment.OnListFragmentInteractionListener {

    private static final String POSITIONITEMKEY = "POSITIONITEMKEY";
    private static final String SAVEBUNDLEKEY = "SAVEBUNDLEKEY";
    private static final int RC_SIGN_IN = 123;

    ParkFragment parkFragment;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (parkFragment != null) {
            getSupportFragmentManager().putFragment(outState, "PARKFRAGMENTKEY", parkFragment);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set default page
        // navigationView.setCheckedItem(R.id.nav_home);
        // navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);

        if (savedInstanceState != null) {
            Log.d("BLA", "LOAD PARKFRAGMENT");
            parkFragment = (ParkFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, "PARKFRAGMENTKEY");
        } else {
            loginUser();
        }
    }

    private void loginUser() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    protected void setupNavHeaderUser(FirebaseUser user) {
        // image
        ImageView navHeaderUserLoginImage = findViewById(R.id.navHeaderUserLoginImage);
        if (user.getPhotoUrl() != null && navHeaderUserLoginImage != null) {
            new ImageLoadTask(user.getPhotoUrl().toString(), navHeaderUserLoginImage).execute();
        }

        // name
        TextView navHeaderUserLoginName = findViewById(R.id.navHeaderUserLoginName);
        if (navHeaderUserLoginImage != null) {
            navHeaderUserLoginName.setText(user.getDisplayName());
        }

        // mail
        TextView navHeaderUserLoginMail = findViewById(R.id.navHeaderUserLoginMail);
        if (navHeaderUserLoginImage != null) {
            navHeaderUserLoginMail.setText(user.getEmail());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                setupNavHeaderUser(user);
            } else {
                signInFailed();
            }
        }
    }


    private void signInFailed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Could not login user");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "Exit app",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        exitApplication();
                    }
                });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    private void exitApplication()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_about);
            navigationView.getMenu().performIdentifierAction(R.id.nav_about, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            analyseNavigationSelect(Integer.toString(id), "nav home");

            if (parkFragment == null) {
                parkFragment = new ParkFragment();
            }

            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(
                    R.id.fragment_holder,
                    parkFragment
            ).addToBackStack(null).commit();
        }
        else if (id == R.id.nav_about) {
            analyseNavigationSelect(Integer.toString(id), "nav about");
            AboutFragment aboutFragment = new AboutFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(
                    R.id.fragment_holder,
                    aboutFragment
            ).addToBackStack(null).commit();
        }/* else if (id == R.id.nav_buy) {

        }*/
        else if (id == R.id.nav_places) {
            analyseNavigationSelect(Integer.toString(id), "nav places");
            PlaceFragment placeFragment = new PlaceFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(
                    R.id.fragment_holder,
                    placeFragment
            ).addToBackStack(null).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void analyseNavigationSelect(String id, String type)
    {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, type);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "NavigationItem");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void showMarkerOnMap(PositionContent.PositionItem item)
    {

        Toast.makeText(this,"clicked.",
                Toast.LENGTH_SHORT).show();

        /*
        Bundle bundle = new Bundle();
        bundle.putSerializable(POSITIONITEMKEY, item);

        ParkFragment parkFragment1 = new ParkFragment();
        parkFragment1.setArguments(bundle);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(
                R.id.fragment_holder,
                parkFragment1
        ).addToBackStack(null).commit();
        */
    }

    @Override
    public void onListFragmentInteraction(PositionContent.PositionItem item) {
        showMarkerOnMap(item);
    }
}