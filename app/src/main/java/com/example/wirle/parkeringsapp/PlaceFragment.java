package com.example.wirle.parkeringsapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wirle.parkeringsapp.dummy.DummyContent;
import com.example.wirle.parkeringsapp.dummy.DummyContent.DummyItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PlaceFragment extends Fragment {

    private static final String DBREFKEY = "DBREFKEY";

    private OnListFragmentInteractionListener mListener;
    private DatabaseReference mDatabaseRef;
    private ArrayList<String> listItems;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // open bundle
        SerializableDatabaseReference serializableDatabaseReference =
                (SerializableDatabaseReference) getArguments()
                .getSerializable(DBREFKEY);

        // init
        initDatabaseReference(serializableDatabaseReference);
        initPositionContent();
        testWriteMsgToDb();
    }

    private void initDatabaseReference(SerializableDatabaseReference
                                               serializableDatabaseReference) {
        mDatabaseRef = serializableDatabaseReference.ref;
    }

    private void initPositionContent() {
        DatabaseReference mDbPositions = mDatabaseRef.child("positions");
        mDbPositions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PositionContent.initPositionContent(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to fetch data",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void testWriteMsgToDb()
    {
        DatabaseReference newPos = mDatabaseRef.child("positions").push();
        PositionContent.PositionItem positionItem = new PositionContent.PositionItem();
        positionItem.id = newPos.getKey();
        positionItem.coordinates = "some coords";
        newPos.setValue(positionItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new PlaceRecyclerViewAdapter(PositionContent.ITEMS, mListener));
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(PositionContent.PositionItem item);
    }
}
