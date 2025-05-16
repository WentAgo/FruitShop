package com.example.fruitshop;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;
    private FirebaseFirestore db; // <-- Added
    private FirebaseAuth mAuth;   // <-- Added
    private static final String TAG = "ItemAdapter_Cart"; // For logging

    public ItemAdapter(List<Item> items) {
        this.items = items;
        this.db = FirebaseFirestore.getInstance(); // Initialize Firestore
        this.mAuth = FirebaseAuth.getInstance();   // Initialize FirebaseAuth
        Log.d(TAG, "Adapter constructor: db and mAuth initialized.");
        if (this.db == null) Log.e(TAG, "Firestore instance is NULL in constructor!");
        if (this.mAuth == null) Log.e(TAG, "FirebaseAuth instance is NULL in constructor!");
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);
        return new ItemViewHolder(view);
    }

    // In ItemViewHolder class:
// public TextWatcher textWatcher;

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item currentItem = items.get(position);
        if (currentItem == null) {
            Log.e(TAG, "Item at position " + position + " is null!");
            return;
        }

        holder.imageView.setImageResource(currentItem.getImageId());
        holder.descriptionTextView.setText(currentItem.getDescription());
        holder.priceTextView.setText(currentItem.getPrice());
        holder.detailsTextView.setText(currentItem.getDetails());

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            holder.priceTextView.setTextColor(Color.WHITE);
            if (holder.quantityEditText != null) {
                holder.quantityEditText.setTextColor(Color.WHITE);
            }
        } else {
            if (holder.quantityEditText != null) {
                holder.quantityEditText.setTextColor(Color.BLACK);
            }
        }

        if (holder.quantityEditText != null) {
            holder.quantityEditText.setText("1");
        }
        holder.quantity = 1;

        if (holder.quantityEditText != null) {
            if (holder.textWatcher != null) {
                holder.quantityEditText.removeTextChangedListener(holder.textWatcher);
            }
            holder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        try {
                            int parsedQuantity = Integer.parseInt(s.toString());
                            if (parsedQuantity < 1) holder.quantity = 1;
                            else if (parsedQuantity > 99) holder.quantity = 99;
                            else holder.quantity = parsedQuantity;
                        } catch (NumberFormatException e) {
                            holder.quantity = 1;
                        }
                    } else {
                        holder.quantity = 1;
                    }
                    // Log.d(TAG, "Holder quantity set to: " + holder.quantity);
                }
            };
            holder.quantityEditText.addTextChangedListener(holder.textWatcher);
        }

        if (holder.addToCartButton != null) {
            holder.addToCartButton.setOnClickListener(v -> {
                Log.d(TAG, "Add to Cart clicked for: " + currentItem.getDescription() + ", quantity: " + holder.quantity);

                FirebaseUser firebaseCurrentUser = mAuth.getCurrentUser();
                if (firebaseCurrentUser == null) {
                    Log.w(TAG, "User not logged in.");
                    Toast.makeText(v.getContext(), "Please log in.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = firebaseCurrentUser.getUid();
                String itemId = currentItem.getItemId(); // Assuming getItemId() returns a non-null, non-empty unique ID

                if (itemId == null || itemId.isEmpty()) {
                    Log.e(TAG, "ItemID is null or empty for item: " + currentItem.getDescription());
                    Toast.makeText(v.getContext(), "Error: Item ID is missing. Cannot add to cart.", Toast.LENGTH_LONG).show();
                    return;
                }

                final int finalQuantityToAdd;
                if (holder.quantity <= 0) {
                    finalQuantityToAdd = 1; // Default to 1 if quantity is invalid
                } else {
                    finalQuantityToAdd = holder.quantity;
                }

                DocumentReference cartItemRef = db.collection("carts").document(userId)
                        .collection("items").document(itemId);

                Log.d(TAG, "Attempting to access Firestore path: " + cartItemRef.getPath());

                cartItemRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && task.getResult().exists()) {
                            // Item already exists in cart, update its quantity
                            Log.d(TAG, "Item " + itemId + " exists in cart. Updating quantity by " + finalQuantityToAdd);
                            cartItemRef.update("quantity", FieldValue.increment(finalQuantityToAdd))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "SUCCESS: Quantity updated for " + itemId);
                                        Toast.makeText(v.getContext(), currentItem.getDescription() + " quantity updated in cart.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "FAILURE: Could not update quantity for " + itemId, e);
                                        Toast.makeText(v.getContext(), "Failed to update cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            // Item does not exist in cart, add it as a new item
                            Log.d(TAG, "Item " + itemId + " does not exist in cart. Adding new item.");
                            Map<String, Object> cartItemData = new HashMap<>();
                            cartItemData.put("itemName", currentItem.getDescription());
                            cartItemData.put("itemId", currentItem.getItemId());

                            // Price parsing
                            String priceString = currentItem.getPrice().replaceAll("[^\\d.]", "");
                            double priceValue = 0.0;
                            if (!priceString.isEmpty()) {
                                try {
                                    priceValue = Double.parseDouble(priceString);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing price: " + currentItem.getPrice() + " for item " + itemId, e);
                                    // Decide how to handle price parsing errors, e.g., skip adding or add with price 0
                                }
                            }
                            cartItemData.put("itemPrice", priceValue);
                            cartItemData.put("quantity", finalQuantityToAdd);
                            cartItemData.put("timestamp", FieldValue.serverTimestamp());

                            // Add imageUrl if available
                            String imageUrl = currentItem.getImageUrl(); // Get it from your Item object
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                cartItemData.put("imageUrl", imageUrl);
                                Log.d(TAG, "Adding imageUrl for " + itemId + ": " + imageUrl);
                            } else {
                                Log.w(TAG, "imageUrl is null or empty for item " + itemId + ". Not adding to cart data.");
                                // Optionally, you could store a default placeholder URL:
                                // cartItemData.put("imageUrl", "your_default_placeholder_image_url");
                            }

                            cartItemRef.set(cartItemData, SetOptions.merge()) // Use merge to be safe, though set should be fine for new doc
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "SUCCESS: Item " + itemId + " added to cart.");
                                        Toast.makeText(v.getContext(), currentItem.getDescription() + " added to cart.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "FAILURE: Could not add item " + itemId + " to cart.", e);
                                        Toast.makeText(v.getContext(), "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        Log.e(TAG, "FAILURE: Error checking cart for item " + itemId, task.getException());
                        Toast.makeText(v.getContext(), "Error checking cart: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView descriptionTextView;
        public TextView priceTextView;
        public TextView detailsTextView;
        public Button addToCartButton;
        public EditText quantityEditText;
        public int quantity;
        public TextWatcher textWatcher;

        public ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImageView);
            descriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            priceTextView = itemView.findViewById(R.id.itemPriceTextView);
            detailsTextView = itemView.findViewById(R.id.itemDetailsTextView);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            quantityEditText = itemView.findViewById(R.id.quantityEditText);
            quantity = 1;
        }
    }
}