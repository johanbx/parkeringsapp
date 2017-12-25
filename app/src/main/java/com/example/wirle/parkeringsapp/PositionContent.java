package com.example.wirle.parkeringsapp;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            addItem(positionItem);
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

    public static class PositionItem {
        public String id;
        public String coordinates;

        /*
        public PositionItem(String id, String coordinates) {
            this.id = id;
            this.coordinates = coordinates;
        }
        */

        @Override
        public String toString() {
            return coordinates;
        }
    }
}