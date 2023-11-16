package com.zdyb.module_diagnosis.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.qmuiteam.qmui.alpha.QMUIAlphaConstraintLayout;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.alpha.QMUIAlphaViewHelper;
import com.qmuiteam.qmui.alpha.QMUIAlphaViewInf;
import com.zdyb.module_diagnosis.R;

public class BottomBarActionButton extends ConstraintLayout {

    //private QMUIAlphaViewHelper mAlphaViewHelper;

    private ImageView mImageview;
    private TextView mTextview;
    private View left_partition,right_partition;
    private ConstraintLayout mainLayout;

    public BottomBarActionButton(Context context) {
        super(context);
        initView(context,null,0);
    }

    public BottomBarActionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs,0);
    }

    public BottomBarActionButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs,defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr){
        LayoutInflater.from(context).inflate(R.layout.widget_bar_action_button,this,true);
        mImageview = findViewById(R.id.icon);
        mTextview = findViewById(R.id.tv_text);
        left_partition = findViewById(R.id.left_partition);
        right_partition = findViewById(R.id.right_partition);

        mainLayout = findViewById(R.id.mainLayout);
    }


    public BottomBarActionButton addValue(int icon,String text){
        mImageview.setImageResource(icon);
        mTextview.setText(text);
        return this;
    }

    public BottomBarActionButton setClick(OnClickListener l){
        mainLayout.setOnClickListener(l);
        return this;
    }

    /**
     * 默认显示右边分割线
     * @param direction LayoutParams.LEFT ，LayoutParams.RIGHT
     */
    public BottomBarActionButton setPartitionLineVisibility(int direction){
        if (direction == LayoutParams.LEFT){
            left_partition.setVisibility(View.VISIBLE);
            right_partition.setVisibility(View.INVISIBLE);
        }else if (direction == LayoutParams.RIGHT){
            right_partition.setVisibility(View.VISIBLE);
            left_partition.setVisibility(View.INVISIBLE);
        }else {
            left_partition.setVisibility(View.INVISIBLE);
            right_partition.setVisibility(View.INVISIBLE);
        }
        return this;
    }

//
//    private QMUIAlphaViewHelper getAlphaViewHelper() {
//        if (mAlphaViewHelper == null) {
//            mAlphaViewHelper = new QMUIAlphaViewHelper(this);
//        }
//        return mAlphaViewHelper;
//    }
//
//    @Override
//    public void setPressed(boolean pressed) {
//        super.setPressed(pressed);
//        getAlphaViewHelper().onPressedChanged(this, pressed);
//    }
//
//    @Override
//    public void setEnabled(boolean enabled) {
//        super.setEnabled(enabled);
//        getAlphaViewHelper().onEnabledChanged(this, enabled);
//    }
//
//    @Override
//    public void setChangeAlphaWhenPress(boolean changeAlphaWhenPress) {
//        getAlphaViewHelper().setChangeAlphaWhenPress(changeAlphaWhenPress);
//    }
//
//    @Override
//    public void setChangeAlphaWhenDisable(boolean changeAlphaWhenDisable) {
//        getAlphaViewHelper().setChangeAlphaWhenDisable(changeAlphaWhenDisable);
//    }
}
