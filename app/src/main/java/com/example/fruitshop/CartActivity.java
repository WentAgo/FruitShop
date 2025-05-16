package com.example.fruitshop;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemInteractionListener {

    private static final String TAG = "CartActivity";

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private Button checkoutButton;
    private Toolbar toolbar;
    private View bottomSummaryLayout;

    private List<CartItem> cartItemList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cart);
        initializeFirebase();

        setupToolbar();

        initializeViews();

        setupRecyclerView();

        setupListeners();

        cartAdapter = new CartAdapter(this, cartItemList, this);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);

        if (currentUser != null) {
            loadCartItems();
        } else {
            Toast.makeText(this, "Please log in to view your cart.", Toast.LENGTH_LONG).show();
            updateUIForEmptyCart();
        }
    }

    private void initializeViews() {

        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        bottomSummaryLayout = findViewById(R.id.bottomSummaryLayout);

        if (totalPriceTextView == null) {
            Log.e(TAG, "totalPriceTextView not found in layout!");
        }
        if (emptyCartTextView == null) {
            Log.e(TAG, "emptyCartTextView not found in layout!");
        }
        if (checkoutButton == null) {
            Log.e(TAG, "checkoutButton not found in layout!");
        }
        if (bottomSummaryLayout == null) {
            Log.e(TAG, "bottomSummaryLayout not found in layout!");
        }
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "Firebase initialized. User: " + currentUser.getUid());
        } else {
            Log.w(TAG, "Firebase initialized. No user currently logged in.");
        }
    }

    private void setupRecyclerView() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        if (cartRecyclerView == null) {
            Log.e(TAG, "cartRecyclerView not found in layout!");
            return;
        }

        cartItemList = new ArrayList<>();

        cartAdapter = new CartAdapter(this, cartItemList, this);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
        Log.d(TAG, "RecyclerView setup complete.");
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.cartToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Shopping Cart");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            Log.e(TAG, "Toolbar not found or support action bar could not be set.");
        }
    }

    private void setupListeners() {
        if (checkoutButton == null) {
            Log.e(TAG, "checkoutButton is null, cannot set listener.");
            return;
        }

        checkoutButton.setOnClickListener(v -> {
            if (cartItemList == null || cartItemList.isEmpty()) {
                Toast.makeText(CartActivity.this, "Your cart is empty. Add items to proceed.", Toast.LENGTH_LONG).show();
            } else {
                String totalPriceString = totalPriceTextView.getText().toString();
                Toast.makeText(CartActivity.this, "Proceeding to checkout... (" + totalPriceString + ")", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Checkout button clicked. Total: " + totalPriceString);
            }
        });

        Log.d(TAG, "Listeners setup complete.");
    }

    private void loadCartItems() {
        if (currentUser == null) {
            Log.w(TAG, "User not logged in. Cannot load cart items.");
            Toast.makeText(this, "Please log in to view your cart.", Toast.LENGTH_LONG).show();
            updateUIForEmptyCart();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Loading cart items for user: " + userId);

        if (emptyCartTextView != null) {
            emptyCartTextView.setText("Loading cart...");
            emptyCartTextView.setVisibility(View.VISIBLE);
        }
        if (cartRecyclerView != null) {
            cartRecyclerView.setVisibility(View.GONE);
        }
        if (bottomSummaryLayout != null) {
            bottomSummaryLayout.setVisibility(View.GONE);
        }


        db.collection("carts").document(userId).collection("items").orderBy("timestamp", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cartItemList.clear();
                QuerySnapshot snapshots = task.getResult();
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (QueryDocumentSnapshot document : snapshots) {
                        CartItem cartItem = document.toObject(CartItem.class);
                        if (cartItem.getItemId() == null || cartItem.getItemId().isEmpty()) {
                            cartItem.setItemId(document.getId());
                        }
                        cartItemList.add(cartItem);
                        Log.d(TAG, "Loaded item: " + cartItem.getItemName() + ", Qty: " + cartItem.getQuantity() + ", Price: " + cartItem.getItemPrice() + ", ID: " + cartItem.getItemId());
                    }
                    Log.d(TAG, "Total items loaded: " + cartItemList.size());
                } else {
                    Log.d(TAG, "Cart is empty for user: " + userId + " or no items found.");
                }
                updateCartUI();
            } else {
                Log.e(TAG, "Error loading cart items: ", task.getException());
                Toast.makeText(CartActivity.this, "Error loading cart items.", Toast.LENGTH_SHORT).show();
                updateUIForEmptyCart();
                if (emptyCartTextView != null) {
                    emptyCartTextView.setText("Could not load cart.");
                }
            }
        });
    }

    private void updateCartUI() {
        if (cartAdapter == null || cartItemList == null) {
            Log.e(TAG, "updateCartUI: cartAdapter or cartItemList is null. Cannot update UI.");
            if (emptyCartTextView != null) {
                emptyCartTextView.setText("Error displaying cart.");
                emptyCartTextView.setVisibility(View.VISIBLE);
            }
            if (cartRecyclerView != null) {
                cartRecyclerView.setVisibility(View.GONE);
            }
            if (bottomSummaryLayout != null) {
                bottomSummaryLayout.setVisibility(View.GONE);
            }
            return;
        }

        cartAdapter.notifyDataSetChanged();

        if (cartItemList.isEmpty()) {
            updateUIForEmptyCart();
        } else {
            if (cartRecyclerView != null) {
                cartRecyclerView.setVisibility(View.VISIBLE);
            }
            if (emptyCartTextView != null) {
                emptyCartTextView.setVisibility(View.GONE);
            }
            if (bottomSummaryLayout != null) {
                bottomSummaryLayout.setVisibility(View.VISIBLE);
            }
            if (checkoutButton != null) {
                checkoutButton.setEnabled(true);
            }
        }
        calculateAndDisplayTotalPrice();
    }

    private void updateUIForEmptyCart() {
        if (cartRecyclerView != null) {
            cartRecyclerView.setVisibility(View.GONE);
        }
        if (emptyCartTextView != null) {
            emptyCartTextView.setText("Your cart is empty.");
            emptyCartTextView.setVisibility(View.VISIBLE);
        }
        if (totalPriceTextView != null) {
            totalPriceTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", 0.0));
        }
        if (checkoutButton != null) {
            checkoutButton.setEnabled(false);
        }
        if (bottomSummaryLayout != null) {
            bottomSummaryLayout.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "UI updated for empty cart.");
    }

    private void calculateAndDisplayTotalPrice() {
        double totalPrice = 0.0;
        if (cartItemList != null) {
            for (CartItem item : cartItemList) {
                if (item != null) {
                    totalPrice += item.getItemPrice() * item.getQuantity();
                }
            }
        }

        if (totalPriceTextView != null) {
            totalPriceTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", totalPrice));
        } else {
            Log.e(TAG, "totalPriceTextView is null, cannot display total price.");
        }
        Log.d(TAG, "Total price calculated and displayed: $" + String.format(Locale.getDefault(), "%.2f", totalPrice));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onChangeQuantityClicked(CartItem item, int position) {
        if (item == null || item.getItemId() == null) {
            Log.e(TAG, "Cannot change quantity, item or itemId is null");
            Toast.makeText(this, "Error: Item data missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "onChangeQuantityClicked for: " + item.getItemName() + " at position " + position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Quantity for " + item.getItemName());

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_change_quantity, null);
        builder.setView(customLayout);

        final EditText editTextQuantity = customLayout.findViewById(R.id.editTextQuantity);
        editTextQuantity.setText(String.valueOf(item.getQuantity()));
        editTextQuantity.setSelection(editTextQuantity.getText().length());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newQuantityStr = editTextQuantity.getText().toString();
            if (newQuantityStr.isEmpty()) {
                Toast.makeText(CartActivity.this, "Quantity cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int newQuantity = Integer.parseInt(newQuantityStr);
                if (newQuantity < 0 || newQuantity > 999) { // Validate quantity
                    Toast.makeText(CartActivity.this, "Quantity must be between 0 and 999.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (newQuantity == 0) {
                    new AlertDialog.Builder(CartActivity.this).setTitle("Remove Item?").setMessage(item.getItemName() + " quantity is 0. Would you like to remove it from the cart?").setPositiveButton("Yes, Remove", (d, w) -> deleteItemFromCart(item, position)).setNeutralButton("Cancel", null).show();
                } else {
                    updateItemQuantityInFirestore(item, newQuantity, position);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(CartActivity.this, "Invalid quantity format.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @Override
    public void onDeleteItemClicked(CartItem item, int position) {
        if (item == null || item.getItemId() == null) {
            Log.e(TAG, "Cannot delete, item or itemId is null");
            Toast.makeText(this, "Error: Item data missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "onDeleteItemClicked for: " + item.getItemName() + " at position " + position);

        new AlertDialog.Builder(this).setTitle("Delete Item").setMessage("Are you sure you want to remove " + item.getItemName() + " from your cart?").setPositiveButton("Delete", (dialog, which) -> {
            deleteItemFromCart(item, position);
        }).setNegativeButton("Cancel", null).show();
    }

    private void updateItemQuantityInFirestore(CartItem item, int newQuantity, int position) {
        if (currentUser == null || item.getItemId() == null) {
            Toast.makeText(this, "Error updating quantity. User or Item ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference itemRef = db.collection("carts").document(userId).collection("items").document(item.getItemId());

        itemRef.update("quantity", newQuantity).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Quantity updated successfully for " + item.getItemName());
            item.setQuantity(newQuantity);
            cartItemList.set(position, item);
            cartAdapter.notifyItemChanged(position);
            calculateAndDisplayTotalPrice();
            Toast.makeText(CartActivity.this, item.getItemName() + " quantity updated.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating quantity for " + item.getItemName(), e);
            Toast.makeText(CartActivity.this, "Failed to update quantity.", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteItemFromCart(CartItem item, int position) {
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to modify the cart.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (item == null || item.getItemId() == null || item.getItemId().isEmpty()) {
            Toast.makeText(this, "Cannot remove item: Invalid item data.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "deleteItemFromCart: item or itemId is null/empty.");
            return;
        }

        String userId = currentUser.getUid();
        String itemId = item.getItemId();

        Log.d(TAG, "Attempting to delete item: " + itemId + " for user: " + userId);

        db.collection("carts").document(userId).collection("items").document(itemId).delete().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Item " + itemId + " successfully deleted from Firestore.");
            if (position >= 0 && position < cartItemList.size()) {
                cartItemList.remove(position);
                cartAdapter.notifyItemRemoved(position);
                cartAdapter.notifyItemRangeChanged(position, cartItemList.size());

                updateCartUI();
                Toast.makeText(CartActivity.this, item.getItemName() + " removed from cart.", Toast.LENGTH_SHORT).show();

                if (cartItemList.isEmpty()) {
                    updateUIForEmptyCart();
                }
            } else {
                Log.w(TAG, "Item deleted from Firestore, but position " + position + " was out of bounds for local list update. List size: " + cartItemList.size());
                loadCartItems();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error deleting item " + itemId + " from Firestore", e);
            Toast.makeText(CartActivity.this, "Failed to remove " + item.getItemName() + ". Please try again.", Toast.LENGTH_SHORT).show();
        });
    }
}