package com.zdyb.lib_common.widget.click;


import android.view.View;

import java.util.concurrent.TimeUnit;

public interface ViewClick extends View.OnClickListener {

    void throttle(long skipDuration, TimeUnit timeUnit);

}