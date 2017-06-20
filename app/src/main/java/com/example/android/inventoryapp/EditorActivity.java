package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EditorActivity.class.getSimpleName();

    private EditText enterItemName;
    private EditText enterItemPrice;
    private EditText enterItemQuantity;
    private ImageView enterItemImage;
    private static final int GET_IMAGE = 1;
    private static int ITEM_LOADER = 1;

    private Uri currentItemUri = null;

    String item_image = null;

    private boolean itemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };
    Uri itemImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentItemUri = intent.getData();

        if (currentItemUri == null) {
            itemImageUri = Uri.parse("android.resource://" + this.getPackageName() + "/drawable/pic");
            setTitle(getString(R.string.add_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_product));
            getLoaderManager().initLoader(ITEM_LOADER, null, this);
        }

        enterItemName = (EditText) findViewById(R.id.enter_item);
        enterItemPrice = (EditText) findViewById(R.id.enter_price);
        enterItemQuantity = (EditText) findViewById(R.id.enter_quantity);
        enterItemImage = (ImageView) findViewById(R.id.added_image);

        enterItemName.setOnTouchListener(mTouchListener);
        enterItemPrice.setOnTouchListener(mTouchListener);
        enterItemQuantity.setOnTouchListener(mTouchListener);

        Button addItemImage = (Button) findViewById(R.id.add_item_image);
        addItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Select Image From"), GET_IMAGE);
                }
            }
        });

        Button addQuantity = (Button) findViewById(R.id.add);
        addQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemQuantity;
                String quantityOrder = enterItemQuantity.getText().toString();
                if (TextUtils.isEmpty(quantityOrder)) {
                    enterItemQuantity.setText("0");
                }
                itemQuantity = Integer.parseInt(enterItemQuantity.getText().toString());
                itemQuantity++;
                enterItemQuantity.setText("" + itemQuantity);
            }
        });

        Button subQuantity = (Button) findViewById(R.id.subtract);
        subQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemQuantity;
                String quantityOrder = enterItemQuantity.getText().toString();
                if (TextUtils.isEmpty(quantityOrder)) {
                    enterItemQuantity.setText("0");
                }
                itemQuantity = Integer.parseInt(enterItemQuantity.getText().toString());
                if (itemQuantity <= 0) {
                    Toast.makeText(EditorActivity.this, "First Buy some item", Toast.LENGTH_SHORT).show();
                } else {
                    itemQuantity--;
                }
                enterItemQuantity.setText("" + itemQuantity);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            item_image = imageUri.toString();
            enterItemImage.setImageURI(imageUri);
        }
    }

    private void saveItem() {
        String nameString = enterItemName.getText().toString().trim();
        String priceString = enterItemPrice.getText().toString().trim();
        String quantityString = enterItemQuantity.getText().toString().trim();
        Log.i(TAG, "Save Item Method Entered");
        String itemImage = itemImageUri.toString();

        if (currentItemUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(itemImage)) {
            Toast.makeText(EditorActivity.this, "Fill All the Details to Store the Item", Toast.LENGTH_SHORT).show();
            return;
        }

        int itemPrice = 0;
        if (!TextUtils.isEmpty(priceString)) {
            itemPrice = Integer.parseInt(priceString);
        }

        int itemQuantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            itemQuantity = Integer.parseInt(quantityString);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        contentValues.put(ItemEntry.COLUMN_ITEM_PRICE, itemPrice);
        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);
        contentValues.put(ItemEntry.COLUMN_ITEM_IMAGE, itemImage);

        if (currentItemUri == null) {

            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, contentValues);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.saving_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.successfully_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentItemUri, contentValues, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_item:
                saveItem();
                finish();
                return true;

            case R.id.delete_item:
                showDeleteConfirmationDialog();
                return true;

            case R.id.order_item:
                submitOrder();
                finish();
                return true;

            case android.R.id.home:

                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };


                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {ItemEntry._ID, ItemEntry.COLUMN_ITEM_NAME, ItemEntry.COLUMN_ITEM_QUANTITY, ItemEntry.COLUMN_ITEM_PRICE, ItemEntry.COLUMN_ITEM_IMAGE};
        return new CursorLoader(this, currentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            enterItemName.setText(name);
            enterItemPrice.setText(Integer.toString(price));
            enterItemQuantity.setText(Integer.toString(quantity));
            itemImageUri = Uri.parse(image);
            enterItemImage = (ImageView) findViewById(R.id.added_image);
            enterItemImage.setImageURI(itemImageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        enterItemName.setText("");
        enterItemPrice.setText("");
        enterItemQuantity.setText("");
        enterItemImage.setImageResource(R.drawable.inventory_icon);
    }

    private void submitOrder() {
        String orderName = enterItemName.getText().toString();
        String orderQuantity = enterItemQuantity.getText().toString();
        Intent orderIntent = new Intent(Intent.ACTION_SENDTO);
        orderIntent.setData(Uri.parse("mailto:ashimaverma2009@gmail.com"));
        orderIntent.putExtra(Intent.EXTRA_TEXT, "Please order " + orderQuantity + " number of " + orderName);
        startActivity(orderIntent);
    }


    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (currentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
