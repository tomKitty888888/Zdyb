package com.zdyb.lib_common.widget;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zdyb.lib_common.databinding.DeviceLoadingBinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * loading
 * author hechao
 * date 2018/9/25 0025
 */
public class DeviceLoadingDialog extends DialogFragment {

    private DeviceLoadingBinding mBinding;
    private AnimationDrawable mAnimation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(true);
        mBinding = DeviceLoadingBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initial();
    }

    private void initial() {
//        mBinding.ivLoadingAnimation.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.device_loading_animation));
//        mAnimation = (AnimationDrawable) mBinding.ivLoadingAnimation.getDrawable();
//        mAnimation.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mAnimation) {
            mAnimation.stop();
            mAnimation = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {

            Window window = getDialog().getWindow();
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.dimAmount = 0.0f;

            window.setAttributes(windowParams);
        }
    }

    /**
     * Display the dialog, adding the fragment to the given FragmentManager.  This
     * is a convenience for explicitly creating a transaction, adding the
     * fragment to it with the given tag, and committing it.  This does
     * <em>not</em> add the transaction to the back stack.  When the fragment
     * is dismissed, a new transaction will be executed to remove it from
     * the activity.
     *
     * @param manager The FragmentManager this fragment will be added to.
     * @param tag     The tag for this fragment, as per
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            Class c = Class.forName("androidx.fragment.app.DialogFragment");
            Constructor con = c.getConstructor();
            Object obj = con.newInstance();
            Field dismissed = c.getDeclaredField("mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(obj, false);
            Field shownByMe = c.getDeclaredField("mShownByMe");
            shownByMe.setAccessible(true);
            shownByMe.set(obj, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isAdded()) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
            manager.executePendingTransactions();
        }
    }

    @Override
    public void dismiss() {
        //super.dismiss();
        dismissAllowingStateLoss();
    }

    public static class Builder {

        private AppCompatActivity mActivity;


        public Builder(AppCompatActivity appCompatActivity) {
            this.mActivity = appCompatActivity;
        }

        public DeviceLoadingDialog build() {
            DeviceLoadingDialog adDialog = new DeviceLoadingDialog();
            return adDialog;
        }

        public void show() {
            DeviceLoadingDialog dialog = build();
            if (null != mActivity.getSupportFragmentManager()) {
                dialog.show(mActivity.getSupportFragmentManager(), "");
            }
        }
    }
}
