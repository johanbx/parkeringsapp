package com.example.wirle.parkeringsapp;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
        PlaceFragment.OnListFragmentInteractionListener,
        PlaceFragment.OnDatabaseRefListener,
        ParkFragment.OnDatabaseRefListener{

    private static final String DBREFKEY = "DBREFKEY";
    private static final int RC_SIGN_IN = 123;

    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabaseRef;

    ParkFragment parkFragment;

    /*
        * Note to self:
        * This function is called on rotation and therefor it will always login and
        * change to "home" on rotation. This is bad practise but will have to suffice for
        * now. For now, users can only use portraitmode (see manifest).
        *
        * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init fragments that require a "state"
        parkFragment = new ParkFragment();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set default page
        navigationView.setCheckedItem(R.id.nav_home);
        navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);

        // Authentication
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN);
    }

    protected void setupNavHeaderUser(FirebaseUser user)
    {
        // image
        ImageView navHeaderUserLoginImage = findViewById(R.id.navHeaderUserLoginImage);
        new ImageLoadTask(user.getPhotoUrl().toString(), navHeaderUserLoginImage).execute();

        // name
        TextView navHeaderUserLoginName = findViewById(R.id.navHeaderUserLoginName);
        navHeaderUserLoginName.setText(user.getDisplayName());

        // mail
        TextView navHeaderUserLoginMail = findViewById(R.id.navHeaderUserLoginMail);
        navHeaderUserLoginMail.setText(user.getEmail());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == ResultCodes.OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                setupNavHeaderUser(user);
                initializeDatabaseConnection(user);
            } else {
                // Sign in failed, check response for error code
                Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeDatabaseConnection(FirebaseUser user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference(user.getUid());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            analyseNavigationSelect(Integer.toString(id), "nav home");
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
        } else if (id == R.id.nav_buy) {

        } else if (id == R.id.nav_places) {
            analyseNavigationSelect(Integer.toString(id), "nav places");
            PlaceFragment placeFragment = new PlaceFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(
                    R.id.fragment_holder,
                    placeFragment
            ).addToBackStack(null).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void analyseNavigationSelect(String id, String type)
    {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, type);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "NavigationItem");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onListFragmentInteraction(PositionContent.PositionItem item) {
        Toast.makeText(this, item.id, Toast.LENGTH_SHORT).show();
    }

    @Override
    public DatabaseReference OnDatabaseRef() {
        return mDatabaseRef;
    }
}