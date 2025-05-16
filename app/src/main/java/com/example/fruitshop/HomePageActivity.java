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

    private static final String LOG_TAG = HomePageActivity.class.getSimpleName();
    private RecyclerView itemsRecyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        setTitle(getString(R.string.app_name));

        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemsList = new ArrayList<>();
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
        int itemId = item.getItemId();

        if (itemId == R.id.cart_menu_item) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_logout) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadItemsFromResources() {
        String[] imageNames = getResources().getStringArray(R.array.item_images);
        String[] descriptions = getResources().getStringArray(R.array.item_descriptions);
        String[] prices = getResources().getStringArray(R.array.item_prices);
        String[] details = getResources().getStringArray(R.array.item_details);

        int minLength = Math.min(imageNames.length, Math.min(descriptions.length, Math.min(prices.length, details.length)));

        for (int i = 0; i < minLength; i++) {
            String itemId = imageNames[i].toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
            String imageNameForDrawable = imageNames[i].toLowerCase(Locale.ROOT);
            String description = descriptions[i];
            String price = prices[i];
            String detail = details[i];

            int imageId = getResources().getIdentifier(imageNameForDrawable, "drawable", getPackageName());
            Log.d(LOG_TAG, "ImageId for " + imageNameForDrawable + " is " + imageId + ", itemId: " + itemId);
            if (imageId != 0) {
                Item item = new Item(itemId, imageId, description, price, detail);
                itemsList.add(item);
            } else {
                Log.e(LOG_TAG, "Could not find resource with name: " + imageNameForDrawable);
            }

        }
    }

}