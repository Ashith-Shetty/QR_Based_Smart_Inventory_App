package com.yourname.qrinventory.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yourname.qrinventory.R;
import com.yourname.qrinventory.model.BasketLine;

import java.util.Locale;
import java.util.Objects;

public class BasketAdapter extends ListAdapter<BasketLine, BasketAdapter.VH> {

    public interface Actions {
        void onQuantityChange(BasketLine line, int newQty);
        void onEdit(BasketLine line);
        void onRemove(BasketLine line);
    }
    private final Actions actions;

    private static final DiffUtil.ItemCallback<BasketLine> DIFF = new DiffUtil.ItemCallback<BasketLine>() {
        @Override public boolean areItemsTheSame(@NonNull BasketLine a, @NonNull BasketLine b) {
            return a.basketId == b.basketId;
        }
        @Override public boolean areContentsTheSame(@NonNull BasketLine a, @NonNull BasketLine b) {
            return a.quantity == b.quantity && a.price == b.price && a.stock == b.stock
                    && Objects.equals(a.name, b.name);
        }
    };

    public BasketAdapter(Actions actions) {
        super(DIFF);
        this.actions = actions;
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, unit, lineTotal, qty;
        final Button minus, plus;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            unit = v.findViewById(R.id.tv_unit);
            lineTotal = v.findViewById(R.id.tv_line_total);
            qty = v.findViewById(R.id.tv_qty);
            minus = v.findViewById(R.id.btn_minus);
            plus = v.findViewById(R.id.btn_plus);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_basket_line, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BasketLine l = getItem(position);
        h.name.setText(l.name);
        h.unit.setText(String.format(Locale.getDefault(), "₹%.2f each", l.price));
        h.lineTotal.setText(String.format(Locale.getDefault(), "₹%.2f", l.price * l.quantity));
        h.qty.setText(String.valueOf(l.quantity));
        h.minus.setOnClickListener(v -> actions.onQuantityChange(l, l.quantity - 1));
        h.plus.setOnClickListener(v -> actions.onQuantityChange(l, l.quantity + 1));

        // Long-press context menu (CO2: menus)
        h.itemView.setOnCreateContextMenuListener((menu, v, info) -> {
            menu.setHeaderTitle(l.name);
            menu.add("Edit quantity").setOnMenuItemClickListener(i -> { actions.onEdit(l); return true; });
            menu.add("Remove").setOnMenuItemClickListener(i -> { actions.onRemove(l); return true; });
        });
    }
}