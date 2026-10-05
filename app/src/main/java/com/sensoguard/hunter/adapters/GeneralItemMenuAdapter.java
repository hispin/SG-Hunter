package com.sensoguard.hunter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.sensoguard.hunter.R;
import com.sensoguard.hunter.classes.GeneralItemMenu;
import com.sensoguard.hunter.interfaces.CallToParentInterface;

import java.util.List;


public class GeneralItemMenuAdapter extends ParentAdapter<GeneralItemMenu> {


    // inspection fix: viewHolder is a local variable, callToParentInterface is final
    private final CallToParentInterface callToParentInterface;

    public GeneralItemMenuAdapter(Context context, int resId, List<GeneralItemMenu> objects, CallToParentInterface callToParentInterface) {
        super(context, resId, objects, callToParentInterface);
        this.callToParentInterface = callToParentInterface;
    }

    @NonNull
    @Override
    public View getView(int position, View rowView, @NonNull ViewGroup parent) {
        final GeneralItemMenu generalItemMenu = getItem(position);
        final ViewHolder viewHolder;
        if (rowView == null) {
            viewHolder = new ViewHolder();
            // inspection fix: LayoutInflater.from never returns null, so getView never returns null
            rowView = LayoutInflater.from(context).inflate(resId, parent, false);
            viewHolder.tvTitleItemMenu = rowView.findViewById(R.id.tvTitleItemMenu);
            viewHolder.ivIconItemMenu = rowView.findViewById(R.id.ivIconItemMenu);
            viewHolder.constrainItemMenu = rowView.findViewById(R.id.constrainItemMenu);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        // inspection fix: getItem can return null
        if (generalItemMenu == null) {
            return rowView;
        }

        if (GeneralItemMenu.selectedItem.equals(generalItemMenu.getValue())) {
            viewHolder.constrainItemMenu.setBackgroundColor(ContextCompat.getColor(context, R.color.gray2));
        } else {
            viewHolder.constrainItemMenu.setBackgroundColor(ContextCompat.getColor(context, R.color.gray1));
        }

        viewHolder.tvTitleItemMenu.setText(generalItemMenu.getTitle());
        viewHolder.ivIconItemMenu.setImageDrawable(ContextCompat.getDrawable(context, generalItemMenu.getIconSmall()));
        viewHolder.constrainItemMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callToParentInterface != null) {
                    GeneralItemMenu.selectedItem = generalItemMenu.getValue();
                    callToParentInterface.selectedItem(generalItemMenu);
                }
                notifyDataSetChanged();
            }
        });

        return rowView;
    }

    public static class ViewHolder {
        TextView tvTitleItemMenu;
        ImageView ivIconItemMenu;
        ConstraintLayout constrainItemMenu;
    }

}
