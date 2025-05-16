package com.example.fruitshop;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Keep for potential one-liners in listener

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Keep for potential one-liners

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemInteractionListener {

    private static final String TAG = "CartActivity";

    // View References
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private Button checkoutButton;
    private Toolbar toolbar;
    private View bottomSummaryLayout;

    // Data
    private List<CartItem> cartItemList;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Essential first call
        // Apply the NoActionBar theme to this Activity via AndroidManifest.xml
        // as discussed to prevent the "IllegalStateException: This Activity already has an action bar..."

        setContentView(R.layout.activity_cart); // Set the layout for the activity

        // Initialize Firebase components (good to do early if needed by other setup)
        initializeFirebase();

        // Find and set up the Toolbar
        // This will call setSupportActionBar(), which needs a NoActionBar theme
        setupToolbar();

        // Find all other views
        initializeViews();

        // Set up the RecyclerView and its adapter
        setupRecyclerView();

        // Set up listeners for buttons, etc.
        setupListeners();

        // Initial UI state:
        // The choice of calling updateUIForEmptyCart() here or within loadCartItems is fine.
        // If currentUser is null, updateUIForEmptyCart() is called, which is good.

        // Load cart items from Firestore
        if (currentUser != null) {
            loadCartItems(); // This will handle UI updates based on loaded data
        } else {
            // Handle case where user is not logged in at activity start
            Toast.makeText(this, "Please log in to view your cart.", Toast.LENGTH_LONG).show();
            updateUIForEmptyCart(); // Show empty cart and disable checkout
        }
    }

    private void initializeViews() {
        // RecyclerView is initialized in setupRecyclerView()
        // Toolbar is initialized in setupToolbar()

        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        bottomSummaryLayout = findViewById(R.id.bottomSummaryLayout);

        // Ensure views are found, log if not (optional, but good for debugging)
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
        cartRecyclerView = findViewById(R.id.cartRecyclerView); // Assuming ID is cartRecyclerView
        if (cartRecyclerView == null) {
            Log.e(TAG, "cartRecyclerView not found in layout!");
            return; // Can't proceed without it
        }

        cartItemList = new ArrayList<>(); // Initialize the list for cart items

        // Initialize the adapter.
        // CartActivity implements CartAdapter.OnCartItemInteractionListener, so 'this' can be passed.
        cartAdapter = new CartAdapter(this, cartItemList, this);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Sets items in a vertical list
        cartRecyclerView.setAdapter(cartAdapter);
        // Optional: Add item decorations like dividers if needed
        // DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(cartRecyclerView.getContext(),
        //        ((LinearLayoutManager) cartRecyclerView.getLayoutManager()).getOrientation());
        // cartRecyclerView.addItemDecoration(dividerItemDecoration);

        Log.d(TAG, "RecyclerView setup complete.");
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.cartToolbar); // Assuming your Toolbar's ID in XML is cartToolbar
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Shopping Cart");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Ensure home is shown as up
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
                // Placeholder for checkout logic
                // For example, navigate to a CheckoutActivity or process payment
                String totalPriceString = totalPriceTextView.getText().toString();
                Toast.makeText(CartActivity.this, "Proceeding to checkout... (" + totalPriceString + ")", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Checkout button clicked. Total: " + totalPriceString);

                // Example: Intent to a new activity (uncomment and create CheckoutActivity.class)
                // Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                // intent.putExtra("TOTAL_PRICE", calculateTotalPrice()); // Pass total if needed
                // startActivity(intent);
            }
        });

        Log.d(TAG, "Listeners setup complete.");
    }

    private void loadCartItems() {
        if (currentUser == null) {
            Log.w(TAG, "User not logged in. Cannot load cart items.");
            Toast.makeText(this, "Please log in to view your cart.", Toast.LENGTH_LONG).show();
            updateUIForEmptyCart(); // Ensure UI reflects empty/inaccessible state
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Loading cart items for user: " + userId);

        // Show a loading state
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


        db.collection("carts").document(userId).collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartItemList.clear(); // Clear previous items before adding new ones
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null && !snapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : snapshots) {
                                CartItem cartItem = document.toObject(CartItem.class);
                                // Firestore automatically maps fields. If your CartItem's itemId field
                                // is named 'itemId', it should map. If not, or if you want to ensure
                                // the document ID is used as the item's ID (useful for updates/deletes):
                                if (cartItem.getItemId() == null || cartItem.getItemId().isEmpty()) {
                                    cartItem.setItemId(document.getId());
                                }
                                cartItemList.add(cartItem);
                                Log.d(TAG, "Loaded item: " + cartItem.getItemName() +
                                        ", Qty: " + cartItem.getQuantity() +
                                        ", Price: " + cartItem.getItemPrice() +
                                        ", ImageURL: " + cartItem.getImageUrl() +
                                        ", ID: " + cartItem.getItemId());
                            }
                            Log.d(TAG, "Total items loaded: " + cartItemList.size());
                        } else {
                            Log.d(TAG, "Cart is empty for user: " + userId + " or no items found.");
                        }
                        updateCartUI(); // Update RecyclerView, total price, and visibility of views
                    } else {
                        Log.e(TAG, "Error loading cart items: ", task.getException());
                        Toast.makeText(CartActivity.this, "Error loading cart items.", Toast.LENGTH_SHORT).show();
                        updateUIForEmptyCart(); // Show empty cart on error too
                        if (emptyCartTextView != null) {
                            emptyCartTextView.setText("Could not load cart.");
                        }
                    }
                });
    }

    private void updateCartUI() {
        // Ensure adapter and list are not null before trying to use them
        if (cartAdapter == null || cartItemList == null) {
            Log.e(TAG, "updateCartUI: cartAdapter or cartItemList is null. Cannot update UI.");
            // Potentially show an error or try to reinitialize if this state is unexpected
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

        cartAdapter.notifyDataSetChanged(); // Tell the adapter data has changed

        if (cartItemList.isEmpty()) {
            updateUIForEmptyCart();
        } else {
            if (cartRecyclerView != null) {
                cartRecyclerView.setVisibility(View.VISIBLE);
            }
            if (emptyCartTextView != null) {
                emptyCartTextView.setVisibility(View.GONE);
            }
            // Ensure the bottom summary layout is visible when there are items
            if (bottomSummaryLayout != null) {
                bottomSummaryLayout.setVisibility(View.VISIBLE);
            }
            if (checkoutButton != null) {
                checkoutButton.setEnabled(true); // Enable checkout if cart not empty
            }
        }
        calculateAndDisplayTotalPrice(); // Always recalculate and display total price
    }

    private void updateUIForEmptyCart() {
        if (cartRecyclerView != null) {
            cartRecyclerView.setVisibility(View.GONE); // Hide the list of items
        }
        if (emptyCartTextView != null) {
            emptyCartTextView.setText("Your cart is empty."); // Set the "empty" message
            emptyCartTextView.setVisibility(View.VISIBLE);    // Show the message
        }
        if (totalPriceTextView != null) {
            // Display $0.00 for the total when the cart is empty
            totalPriceTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", 0.0));
        }
        if (checkoutButton != null) {
            checkoutButton.setEnabled(false); // Disable the checkout button
        }
        // Decide on the visibility of the bottom summary layout when the cart is empty.
        // Option 1: Hide it completely.
        // if (bottomSummaryLayout != null) {
        //     bottomSummaryLayout.setVisibility(View.GONE);
        // }
        // Option 2: Keep it visible (to show the $0.00 total) but with the button disabled.
        // This is often preferred for consistency in layout.
        if (bottomSummaryLayout != null) {
            bottomSummaryLayout.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "UI updated for empty cart.");
    }

    private void calculateAndDisplayTotalPrice() {
        double totalPrice = 0.0;
        if (cartItemList != null) { // Ensure the list itself is not null
            for (CartItem item : cartItemList) {
                if (item != null) { // Ensure the item in the list is not null
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
        // Handle action bar item clicks here.
        // The action bar will automatically handle clicks on the Home/Up button
        // if you've set setDisplayHomeAsUpEnabled(true) in setupToolbar()
        // and specified a parent activity in AndroidManifest.xml (optional for simple back).

        if (item.getItemId() == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of a PUSHED screen,
            // it behaves like the back button.
            // NavUtils.navigateUpFromSameTask(this); // More complex back navigation
            finish(); // Simply finishes the current activity and returns to the previous one.
            return true; // Indicates that the event was handled.
        }
        return super.onOptionsItemSelected(item); // Delegate to parent class for other menu items.
    }

    // --- OnCartItemInteractionListener Implementation ---

    @Override
    public void onActionButton1Clicked(CartItem item, int position) {
        // Content will be provided later
        // Example: Toast.makeText(this, item.getItemName() + " Action 1", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionButton2Clicked(CartItem item, int position) {
        // Content will be provided later
        // Example: Toast.makeText(this, item.getItemName() + " Action 2", Toast.LENGTH_SHORT).show();
    }

    private void removeItemFromCart(CartItem item, int position) {
        // Content will be provided later
    }
}