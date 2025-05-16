package com.example.fruitshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomePageActivity extends AppCompatActivity {

    private RecyclerView itemsRecyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemsList;
    private static final String LOG_TAG = HomePageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //set the title in the action bar
        setTitle(getString(R.string.app_name));

        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the items list
        itemsList = new ArrayList<>();
        // Load data from string arrays
        loadItemsFromResources();

        itemAdapter = new ItemAdapter(itemsList);
        itemsRecyclerView.setAdapter(itemAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId(); // Get the ID of the selected item

        if (itemId == R.id.cart_menu_item) { // <<<< ADD THIS CASE
            // User clicked the cart icon, open CartActivity
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true; // Event handled
        } else if (itemId == R.id.menu_logout) {
            // Navigate back to MainActivity (login screen)
            Intent intent = new Intent(this, MainActivity.class);
            // Clear activity stack so the user can't go back to this activity using the back button
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true; // Event handled
        }
        // If you had an "Up" button in HomePageActivity's toolbar, you'd handle android.R.id.home here
        // else if (itemId == android.R.id.home) {
        //     finish(); // Or NavUtils.navigateUpFromSameTask(this);
        //     return true;
        // }

        return super.onOptionsItemSelected(item); // Delegate to parent for any other unhandled items
    }
    private void loadItemsFromResources() {
        // Get the string arrays from resources
        String[] imageNames = getResources().getStringArray(R.array.item_images);
        String[] descriptions = getResources().getStringArray(R.array.item_descriptions);
        String[] prices = getResources().getStringArray(R.array.item_prices);
        String[] details = getResources().getStringArray(R.array.item_details);

        // We need to get the minimum length of all arrays
        int minLength = Math.min(imageNames.length, Math.min(descriptions.length, Math.min(prices.length, details.length)));

        // Create items and add them to the list
        for (int i = 0; i < minLength; i++) {
            String itemId = imageNames[i].toLowerCase(Locale.ROOT).replaceAll("\\s+", "_"); // Sanitize a bit
            String imageNameForDrawable = imageNames[i].toLowerCase(Locale.ROOT); // For looking up drawable
            String description = descriptions[i];
            String price = prices[i];
            String detail = details[i];

            // Use getIdentifier to get the resource ID dynamically
            int imageId = getResources().getIdentifier(imageNameForDrawable, "drawable", getPackageName());
            Log.d(LOG_TAG, "ImageId for " + imageNameForDrawable + " is " + imageId + ", itemId: " + itemId);
            // Check if the resource was found
            if (imageId != 0) {
                // Create Item object with the new itemId
                Item item = new Item(itemId, imageId, description, price, detail);
                itemsList.add(item);
            } else {
                Log.e(LOG_TAG, "Could not find resource with name: " + imageNameForDrawable);
            }

        }
    }

}