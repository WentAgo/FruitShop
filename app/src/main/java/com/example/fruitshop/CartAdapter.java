package com.example.fruitshop;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Import Glide
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList;
    private Context context; // Needed for Glide and Toasts

    // Optional: Listener for item interactions
    public interface OnCartItemInteractionListener {
        void onChangeQuantityClicked(CartItem item, int position); // To initiate quantity change
        void onDeleteItemClicked(CartItem item, int position);    // To initiate deletion
        // You might have other methods here like onActionButton1Clicked,
        // you can either rename them or add these new specific ones.
        // For clarity, let's assume you're adding these specific ones.
    }

    private OnCartItemInteractionListener listener;

    // Constructor with listener
    public CartAdapter(Context context, List<CartItem> cartItemList, OnCartItemInteractionListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    // Simpler constructor without a specific listener (uses Toasts)
    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = null; // No specific listener, will use default Toast behavior
    }


    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_row, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem currentCartItem = cartItemList.get(position);

        if (currentCartItem == null) {
            // Should not happen if list is managed properly, but good for safety
            Log.e("CartAdapter", "currentCartItem is null at position " + position);
            // Optionally set views to some default error state or return
            return;
        }

        // 1. Set Textual Data
        holder.descriptionTextView.setText(currentCartItem.getItemName());
        holder.priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", currentCartItem.getItemPrice()));
        holder.quantityTextView.setText(String.format(Locale.getDefault(), "Qty: %d", currentCartItem.getQuantity()));

        // Handle detailsTextView (assuming it might not always be directly in CartItem or used)
        // If your CartItem has a specific 'details' field you want to show, use it.
        // Otherwise, you might clear it, hide it, or set a generic description.
        // For now, let's assume details are part of itemName or not shown separately in the cart row.
        if (holder.detailsTextView != null) {
            // If you have a getDetails() in CartItem:
            // if (currentCartItem.getDetails() != null && !currentCartItem.getDetails().isEmpty()) {
            //    holder.detailsTextView.setText(currentCartItem.getDetails());
            //    holder.detailsTextView.setVisibility(View.VISIBLE);
            // } else {
            //    holder.detailsTextView.setVisibility(View.GONE); // Or set some default text
            // }
            // For now, if cart_item_row.xml has detailsTextView but CartItem doesn't provide specific details for it:
            holder.detailsTextView.setVisibility(View.GONE); // Or setText("")
        }


        // 2. Image Loading Logic (Focus on itemId for local drawables)
        String itemId = currentCartItem.getItemId(); // This comes from Firestore

        Log.d("CartAdapter", "Binding item: " + currentCartItem.getItemName() + ", Position: " + position + ", ItemID from CartItem: '" + itemId + "'");

        Context context = holder.imageView.getContext(); // Get context from one of the views in holder

        if (itemId != null && !itemId.isEmpty()) {
            // Assuming itemId is already in the correct format to match drawable names
            // (e.g., "apple" for "apple.png"). If not, apply transformations here.
            // String drawableName = itemId.toLowerCase(Locale.ROOT).replace(" ", "_"); // Example transformation
            String drawableName = itemId; // Use directly if itemId in DB matches drawable name format

            int imageResourceId = context.getResources().getIdentifier(
                    drawableName,
                    "drawable",
                    context.getPackageName()
            );

            Log.d("CartAdapter", "Attempting to load drawable. Name derived from itemId: '" + drawableName + "'. Found Resource ID: " + imageResourceId);

            if (imageResourceId != 0) { // Resource ID found
                Glide.with(context)
                        .load(imageResourceId) // Load from resource ID
                        .placeholder(R.drawable.default_image_placeholder) // Replace with your actual placeholder
                        .error(R.drawable.default_image_placeholder)       // Replace with your actual error drawable
                        .into(holder.imageView);
            } else {
                // Drawable resource NOT found based on itemId
                Log.w("CartAdapter", "Drawable resource not found for name: '" + drawableName + "' (derived from itemId: '" + itemId + "')");
                Glide.with(context)
                        .load(R.drawable.default_image_placeholder) // Show error/default image
                        .into(holder.imageView);
            }
        } else {
            // itemId itself is null or empty in the CartItem from Firestore
            Log.w("CartAdapter", "ItemID is null or empty for item: " + currentCartItem.getItemName());
            Glide.with(context)
                    .load(R.drawable.default_image_placeholder) // Show placeholder
                    .into(holder.imageView);
        }

        // 3. Handle Button Clicks (using listener or default Toast)
        holder.button1.setText("Change Qty"); // Or use an icon
        holder.button1.setOnClickListener(v -> {
            if (listener != null) {
                // Pass the current item and its adapter position
                listener.onChangeQuantityClicked(currentCartItem, holder.getAdapterPosition());
            }
        });

        holder.button2.setText("Delete"); // Or use an icon
        holder.button2.setOnClickListener(v -> {
            if (listener != null) {
                // Pass the current item and its adapter position
                listener.onDeleteItemClicked(currentCartItem, holder.getAdapterPosition());
            }
        });
    }
    @Override
    public int getItemCount() {
        return cartItemList == null ? 0 : cartItemList.size();
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItemList.clear();
        if (newCartItems != null) {
            this.cartItemList.addAll(newCartItems);
        }
        notifyDataSetChanged(); // Or use DiffUtil for better performance
    }

    public double calculateTotalPrice() {
        double totalPrice = 0;
        if (cartItemList != null) {
            for (CartItem item : cartItemList) {
                totalPrice += item.getItemPrice() * item.getQuantity();
            }
        }
        return totalPrice;
    }


    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionTextView;
        TextView priceTextView;
        TextView detailsTextView; // Make sure this ID matches your cart_item_row.xml
        TextView quantityTextView;
        Button button1;
        Button button2;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cartItemImageView);
            descriptionTextView = itemView.findViewById(R.id.cartItemDescriptionTextView);
            priceTextView = itemView.findViewById(R.id.cartItemPriceTextView);
            detailsTextView = itemView.findViewById(R.id.cartItemDetailsTextView); // Check this ID
            quantityTextView = itemView.findViewById(R.id.cartItemQuantityTextView);
            button1 = itemView.findViewById(R.id.cartItemButton1);
            button2 = itemView.findViewById(R.id.cartItemButton2);
        }
    }
}