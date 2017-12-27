package com.example.wirle.parkeringsapp;

import com.google.android.gms.games.snapshot.Snapshot;
import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.location.Address;
import android.location.Location;

/**
 * Created by johan on 2017-12-25.
 */

public class PositionContent {

    public static final List<PositionItem> ITEMS = new ArrayList<>();
    public static final Map<String, PositionItem> ITEM_MAP = new HashMap<>();

    public static void initPositionContent(DataSnapshot snapshot){
        clearItems();

        for (DataSnapshot positionSnapshot: snapshot.getChildren()) {
            PositionItem positionItem = positionSnapshot.getValue(PositionItem.class);
            insertItem(positionItem);
        }
    }

    private static void clearItems()
    {
        ITEM_MAP.clear();
        ITEMS.clear();
    }

    private static void addItem(PositionItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static void insertItem(PositionItem item) {
        ITEMS.add(0, item);
        ITEM_MAP.put(item.id, item);
    }

    public static class PositionItem implements Serializable {
        public String id;
        public Double longitude;
        public Double latitude;
        public Long time;
        public Float accuracy;
        public String address;

        @Override
        public String toString() {
            return (new Timestamp(time)).toString() + " " + address;
        }
    }
}