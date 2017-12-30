package com.example.wirle.parkeringsapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment implements View.OnClickListener {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        // set button click listeners
        ImageButton facebookButton = v.findViewById(R.id.facebook_logo_button);
        facebookButton.setOnClickListener(this);
        ImageButton twitterButton = v.findViewById(R.id.twitter_logo_button);
        twitterButton.setOnClickListener(this);
        ImageButton googleButton = v.findViewById(R.id.google_plus_logo_button);
        googleButton.setOnClickListener(this);


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onClick(View view) {
        Log.d("BLA", "YOU CLICKED!");
        //do what you want to do when button is clicked
        switch (view.getId()) {
            case R.id.facebook_logo_button:
                Toast.makeText(getActivity(),"Go to Facebook",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.twitter_logo_button:
                Toast.makeText(getActivity(),"Go to Twitter",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.google_plus_logo_button:
                Toast.makeText(getActivity(),"Go to Google Plus",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
