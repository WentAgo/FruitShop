package com.example.fruitshop;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItemList;
    private final Context context;
    private final OnCartItemInteractionListener listener;

    public CartAdapter(Context context, List<CartItem> cartItemList, OnCartItemInteractionListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = null;
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
            Log.e("CartAdapter", "currentCartItem is null at position " + position);
            return;
        }

        holder.descriptionTextView.setText(currentCartItem.getItemName());
        holder.priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", currentCartItem.getItemPrice()));
        holder.quantityTextView.setText(String.format(Locale.getDefault(), "Qty: %d", currentCartItem.getQuantity()));

        if (holder.detailsTextView != null) {
            holder.detailsTextView.setVisibility(View.GONE);
        }

        String itemId = currentCartItem.getItemId();

        Log.d("CartAdapter", "Binding item: " + currentCartItem.getItemName() + ", Position: " + position + ", ItemID from CartItem: '" + itemId + "'");

        Context context = holder.imageView.getContext();

        if (itemId != null && !itemId.isEmpty()) {
            String drawableName = itemId;

            int imageResourceId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());

            Log.d("CartAdapter", "Attempting to load drawable. Name derived from itemId: '" + drawableName + "'. Found Resource ID: " + imageResourceId);

            if (imageResourceId != 0) {
                Glide.with(context).load(imageResourceId).placeholder(R.drawable.default_image_placeholder).error(R.drawable.default_image_placeholder).into(holder.imageView);
            } else {
                Log.w("CartAdapter", "Drawable resource not found for name: '" + drawableName + "' (derived from itemId: '" + itemId + "')");
                Glide.with(context).load(R.drawable.default_image_placeholder).into(holder.imageView);
            }
        } else {
            Log.w("CartAdapter", "ItemID is null or empty for item: " + currentCartItem.getItemName());
            Glide.with(context).load(R.drawable.default_image_placeholder).into(holder.imageView);
        }

        holder.button1.setText("Change Qty");
        holder.button1.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeQuantityClicked(currentCartItem, holder.getAdapterPosition());
            }
        });

        holder.button2.setText("Delete");
        holder.button2.setOnClickListener(v -> {
            if (listener != null) {
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
        notifyDataSetChanged();
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

    public interface OnCartItemInteractionListener {
        void onChangeQuantityClicked(CartItem item, int position);

        void onDeleteItemClicked(CartItem item, int position);
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionTextView;
        TextView priceTextView;
        TextView detailsTextView;
        TextView quantityTextView;
        Button button1;
        Button button2;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cartItemImageView);
            descriptionTextView = itemView.findViewById(R.id.cartItemDescriptionTextView);
            priceTextView = itemView.findViewById(R.id.cartItemPriceTextView);
            detailsTextView = itemView.findViewById(R.id.cartItemDetailsTextView);
            quantityTextView = itemView.findViewById(R.id.cartItemQuantityTextView);
            button1 = itemView.findViewById(R.id.cartItemButton1);
            button2 = itemView.findViewById(R.id.cartItemButton2);
        }
    }
}