package com.example.fruitshop;

import android.content.Context;
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
        void onActionButton1Clicked(CartItem item, int position);
        void onActionButton2Clicked(CartItem item, int position);
        // Add other interactions like removeItem if needed
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
            return; // Should not happen if list is managed properly
        }

        holder.descriptionTextView.setText(currentCartItem.getItemName());
        holder.priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", currentCartItem.getItemPrice()));
        holder.quantityTextView.setText(String.format(Locale.getDefault(), "Qty: %d", currentCartItem.getQuantity()));

        // Use a placeholder if details are not part of CartItem or not always present
        // For now, assuming CartItem doesn't have 'details'. If it does, uncomment and use:
        // holder.detailsTextView.setText(currentCartItem.getDetails());
        // If details are not in CartItem, you might want to hide this TextView or set default text.
        if (holder.detailsTextView != null) {
            // Example: if CartItem has a getDetails method
            // if (currentCartItem.getDetails() != null && !currentCartItem.getDetails().isEmpty()) {
            //    holder.detailsTextView.setText(currentCartItem.getDetails());
            //    holder.detailsTextView.setVisibility(View.VISIBLE);
            // } else {
            //    holder.detailsTextView.setVisibility(View.GONE); // Or set some default text
            // }
            // For now, let's assume details aren't directly on CartItem or it's optional
            // and might be handled by the data you put into itemName.
            // If you want details here, ensure CartItem has a getDetails() method and it's populated.
            // For the layout you provided (cart_item_row.xml based on items.xml), it expects details.
            // If your CartItem doesn't have details, this will show "@string/details".
            // You might want to fetch full item details or simplify the cart_item_row.
            // Let's assume for now CartItem might not have details, so we clear it or hide it.
            holder.detailsTextView.setText(""); // Clear it or set specific cart item details if available
        }


        // Load image using Glide
        if (currentCartItem.getImageUrl() != null && !currentCartItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(currentCartItem.getImageUrl())
                    .placeholder(R.drawable.default_image_placeholder) // Optional: a placeholder drawable
                    .error(R.drawable.default_image_placeholder)       // Optional: an error drawable
                    .into(holder.imageView);
        } else {
            // Set a default image or hide ImageView if no URL
            holder.imageView.setImageResource(R.drawable.default_image_placeholder); // Replace with your placeholder
        }

        // Handle Button 1 click
        holder.button1.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActionButton1Clicked(currentCartItem, holder.getAdapterPosition());
            } else {
                Toast.makeText(context, currentCartItem.getItemName() + ": Action 1 clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Button 2 click
        holder.button2.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActionButton2Clicked(currentCartItem, holder.getAdapterPosition());
            } else {
                Toast.makeText(context, currentCartItem.getItemName() + ": Action 2 clicked", Toast.LENGTH_SHORT).show();
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