package com.example.fruitshop;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.imageView.setImageResource(item.getImageId());
        holder.descriptionTextView.setText(item.getDescription());
        holder.priceTextView.setText(item.getPrice());
        holder.detailsTextView.setText(item.getDetails());
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            holder.priceTextView.setTextColor(Color.WHITE);
            holder.quantityEditText.setTextColor(Color.WHITE);
        }else{
            holder.quantityEditText.setTextColor(Color.BLACK);
        }
        holder.quantityEditText.setText("1"); // Initialize quantity to 1
        holder.quantity = 1;
        holder.quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int quantity = Integer.parseInt(s.toString());
                    if (quantity < 1) {
                        holder.quantityEditText.setText("1");
                        holder.quantity = 1;
                    } else if (quantity > 99) {
                        holder.quantityEditText.setText("99");
                        holder.quantity = 99;
                    } else {
                        holder.quantity = quantity;
                    }
                } else {
                    holder.quantity = 1;
                }
            }
        });

        holder.addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO

                Toast.makeText(v.getContext(), "Added " + holder.quantity + " " + item.getDescription() + "(s) to the cart", Toast.LENGTH_SHORT).show();
            }
        });
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