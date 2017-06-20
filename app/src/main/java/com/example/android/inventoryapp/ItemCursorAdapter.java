package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Created by Om on 25-Apr-17.
 */

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);

        final int item_id = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry._ID));

        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

        String item_name = cursor.getString(nameColumnIndex);
        final int item_price = cursor.getInt(priceColumnIndex);
        final int item_quantity = cursor.getInt(quantityColumnIndex);

        Button sell = (Button) view.findViewById(R.id.sell);
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = item_quantity;
                if (quantity <= 0) {
                    Toast.makeText(context, "Out of Stock", Toast.LENGTH_SHORT).show();
                } else {
                    quantity--;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

                Uri uri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, item_id);

                context.getContentResolver().update(uri, contentValues, null, null);

                quantityTextView.setText(item_quantity + " Items are there");
            }
        });

        nameTextView.setText("Item Name: " + item_name);
        priceTextView.setText("Rs. " + item_price);
        quantityTextView.setText(item_quantity + " items Left");
    }

}
