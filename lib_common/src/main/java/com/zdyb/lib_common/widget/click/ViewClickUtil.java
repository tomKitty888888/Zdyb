package com.zdyb.lib_common.widget.click;

import android.view.View;

import java.util.concurrent.TimeUnit;

public final class ViewClickUtil {

    private long mSkipDuration;
    private TimeUnit mTimeUnit;
    private ViewClick mViewClick;
    private Type mType;

    private ViewClickUtil(Builder builder) {

        mSkipDuration = builder.mSkipDuration;
        mTimeUnit = builder.mTimeUnit;
        mType = builder.mType;

    }

    public void clicks(View.OnClickListener onClickListener, View view) {

        switch (mType) {
            case RX_VIEW:
                //mViewClick = new RxViewClickImp(view, onClickListener);
                break;

            case VIEW:
                mViewClick = new ViewClickImp(view, onClickListener);

                break;

        }

        mViewClick.throttle(mSkipDuration, mTimeUnit);

    }

    public void clicks(View.OnClickListener onClickListener, View... views) {

        for (View view : views) {
            clicks(onClickListener, view);
        }

    }

    public static class Builder {

        private long mSkipDuration;
        private TimeUnit mTimeUnit;
        private Type mType = Type.VIEW;

        public Builder setSkipDuration(long mSkipDuration) {
            this.mSkipDuration = mSkipDuration;
            return this;
        }

        public Builder setTimeUnit(TimeUnit mTimeUnit) {
            this.mTimeUnit = mTimeUnit;
            return this;
        }

        public Builder setType(Type type) {
            this.mType = type;
            return this;
        }

        public ViewClickUtil build() {
            return new ViewClickUtil(this);
        }

    }

    public enum Type {
        RX_VIEW, VIEW
    }

}