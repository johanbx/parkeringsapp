package com.example.wirle.parkeringsapp;

import com.google.firebase.database.DatabaseReference;
import java.io.Serializable;

/**
 * Created by johan on 2017-12-23.
 */

public class SerializableDatabaseReference implements Serializable {
    DatabaseReference ref;
    SerializableDatabaseReference(DatabaseReference ref_){
        ref = ref_;
    }
}
