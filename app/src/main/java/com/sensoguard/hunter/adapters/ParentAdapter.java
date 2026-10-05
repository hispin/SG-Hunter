package com.sensoguard.hunter.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import com.sensoguard.hunter.interfaces.CallToParentInterface;

import java.util.ArrayList;
import java.util.List;


public class ParentAdapter<T> extends ArrayAdapter<T> {
    // inspection fix: final fields
    protected final int resId;
    protected final Context context;
    CallToParentInterface callToParentInterface;

    public ParentAdapter(Context context, int resId, List<T> objects) {
        super(context, resId, objects);
        this.resId = resId;
        this.context = context;
    }

    public ParentAdapter(Context context, int resId, List<T> objects, CallToParentInterface callToParentInterface) {
        super(context, resId, objects);
        this.resId = resId;
        this.context = context;
        this.callToParentInterface = callToParentInterface;
    }


    // inspection fix: removed getCount override that only called super

    @Nullable
    @Override
    public T getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(T item) {
        return super.getPosition(item);
    }

    public void setItems(ArrayList<T> arrZones) {
        clear();
        addAll(arrZones);
    }
}
