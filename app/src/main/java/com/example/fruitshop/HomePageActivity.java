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
        if (item.getItemId() == R.id.menu_logout) {
            // Navigate back to MainActivity (login screen)
            Intent intent = new Intent(this, MainActivity.class);
            // Clear activity stack so the user can't go back to this activity using the back button
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            String imageName = imageNames[i].toLowerCase(); //convert to lower case to avoid problems
            String description = descriptions[i];
            String price = prices[i];
            String detail = details[i];

            // Use getIdentifier to get the resource ID dynamically
            int imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            Log.d(LOG_TAG, "ImageId for "+imageName+" is " + imageId);

            // Check if the resource was found
            if (imageId != 0) {
                Item item = new Item(imageId,description, price, detail);
                itemsList.add(item);
            } else {
                Log.e(LOG_TAG, "Could not find resource with name: " + imageName);
            }

        }
    }

}