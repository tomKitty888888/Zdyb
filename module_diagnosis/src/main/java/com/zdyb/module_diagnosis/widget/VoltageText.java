package com.zdyb.module_diagnosis.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zdyb.module_diagnosis.R;

public class VoltageText extends ConstraintLayout {


    private TextView mIndex,mValue;

    private ConstraintLayout mainLayout;

    public VoltageText(Context context) {
        super(context);
        initView(context,null,0);
    }

    public VoltageText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs,0);
    }

    public VoltageText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs,defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr){
        LayoutInflater.from(context).inflate(R.layout.item_widget_voltage_1,this,true);
        mIndex = findViewById(R.id.index);
        mValue = findViewById(R.id.value);


        mainLayout = findViewById(R.id.mainLayout);
    }


    public VoltageText setIndexValue(String value,int color){
        mValue.setText(value);
        mValue.setTextColor(color);
        return this;
    }

    public VoltageText setClick(OnClickListener l){
        mainLayout.setOnClickListener(l);
        return this;
    }


}
