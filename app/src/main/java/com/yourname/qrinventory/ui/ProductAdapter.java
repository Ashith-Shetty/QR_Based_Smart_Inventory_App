package com.yourname.qrinventory.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yourname.qrinventory.R;
import com.yourname.qrinventory.model.Product;

import java.util.Locale;
import java.util.Objects;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.VH> {

    public interface OnProductClick { void onClick(Product p); }
    private final OnProductClick listener;

    private static final DiffUtil.ItemCallback<Product> DIFF = new DiffUtil.ItemCallback<Product>() {
        @Override public boolean areItemsTheSame(@NonNull Product a, @NonNull Product b) {
            return a.id == b.id;
        }
        @Override public boolean areContentsTheSame(@NonNull Product a, @NonNull Product b) {
            return Objects.equals(a.name, b.name) && Objects.equals(a.category, b.category)
                    && a.price == b.price && a.stock == b.stock
                    && a.lowStockThreshold == b.lowStockThreshold;
        }
    };

    public interface MenuActions {
        void onEdit(Product p);
        void onShowQr(Product p);
        void onDelete(Product p);
    }
    private MenuActions menuActions;
    public void setMenuActions(MenuActions m) { this.menuActions = m; }

    public ProductAdapter(OnProductClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, category, low, price, stock;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            category = v.findViewById(R.id.tv_category);
            low = v.findViewById(R.id.tv_low);
            price = v.findViewById(R.id.tv_price);
            stock = v.findViewById(R.id.tv_stock);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = getItem(position);
        h.name.setText(p.name);
        h.category.setText(p.category);
        h.price.setText(String.format(Locale.getDefault(), "₹%.2f", p.price));
        h.stock.setText("Stock: " + p.stock);
        if (p.stock <= 0) {
            h.low.setText("Out of stock");
            h.low.setVisibility(View.VISIBLE);
        } else if (p.stock <= p.lowStockThreshold) {
            h.low.setText("Low stock");
            h.low.setVisibility(View.VISIBLE);
        } else {
            h.low.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> listener.onClick(p));
        h.itemView.setOnCreateContextMenuListener((menu, v, info) -> {
            if (menuActions == null) return;
            menu.setHeaderTitle(p.name);
            menu.add("Edit product").setOnMenuItemClickListener(i -> { menuActions.onEdit(p); return true; });
            menu.add("Show QR code").setOnMenuItemClickListener(i -> { menuActions.onShowQr(p); return true; });
            menu.add("Delete").setOnMenuItemClickListener(i -> { menuActions.onDelete(p); return true; });
        });
    }
}