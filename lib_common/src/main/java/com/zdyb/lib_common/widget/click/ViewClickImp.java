package com.zdyb.lib_common.widget.click;


import android.os.SystemClock;
import android.view.View;

import java.util.concurrent.TimeUnit;

public class ViewClickImp implements ViewClick{

    private View mView;

    private View.OnClickListener mOnClickListener;

    private long mOldTime;

    private long mDelayMilliseconds = 1000;

    public ViewClickImp(View view, View.OnClickListener onClickListener) {

        mView = view;
        mOnClickListener = onClickListener;
        mView.setOnClickListener(this);

    }

    @Override

    public void onClick(final View v) {

        long nowTime = SystemClock.elapsedRealtime();
        long intervalTime = nowTime - mOldTime;
        if (mOldTime == 0 || intervalTime >= mDelayMilliseconds) {
            mOldTime = nowTime;
            mOnClickListener.onClick(v);
        }

    }

    @Override

    public void throttle(long skipDuration, TimeUnit timeUnit) {

        if (skipDuration < 0) {
            skipDuration = 0;
        }

        mDelayMilliseconds = timeUnit.toMillis(skipDuration);

    }
}
