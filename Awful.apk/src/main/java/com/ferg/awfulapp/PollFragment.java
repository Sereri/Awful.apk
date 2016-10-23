/********************************************************************************
 * Copyright (c) 2012, Matthew Shepard
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the software nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.ferg.awfulapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ferg.awfulapp.constants.Constants;
import com.ferg.awfulapp.preferences.AwfulPreferences;
import com.ferg.awfulapp.thread.AwfulPoll;
import com.ferg.awfulapp.thread.AwfulThread;
import com.ferg.awfulapp.util.AwfulUtils;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;

public class PollFragment extends AwfulDialogFragment {
    private final static String TAG = "PollFragment";

    private HorizontalBarChart barChart;
    private View dialogView;
    private ProgressBar progressBar;

    HashMap<String, String> preferences;

    @Override
    public void onActivityCreated(Bundle aSavedState) {
        super.onActivityCreated(aSavedState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialogView = inflateView(R.layout.poll_view, container, inflater);
        barChart = (HorizontalBarChart) dialogView.findViewById(R.id.poll_chart);
        progressBar = (ProgressBar) dialogView.findViewById(R.id.preview_progress);

        getDialog().setCanceledOnTouchOutside(true);

        return dialogView;
    }

    public void setPoll(AwfulPoll poll){
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;
        for(AwfulPoll.AwfulPollItem item : poll.getItems()){
            entries.add(new BarEntry(index++,item.getVotes()));
            labels.add(item.getOptionTitle());
        }
        final String[] labelStrings = labels.toArray(new String[]{});
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        set.setDrawValues(true);
        BarData data = new BarData(set);
        data.setValueTextColor(Color.RED);
        data.setHighlightEnabled(false);
        data.setBarWidth(0.9f);
        data.setValueTextSize(10f);
        data.setValueTypeface(Typeface.DEFAULT);
        barChart.setDrawValueAboveBar(true);
        barChart.setData(data);
        barChart.setScaleEnabled(false);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.setVisibleXRange(0,1);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDescription("");
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.getXAxis().setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return labelStrings[(int) value % labelStrings.length];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setGranularity(1);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setAxisMinValue(0);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.animateY(1400, Easing.EasingOption.EaseInOutQuart);
        barChart.getLegend().setEnabled(false);
        barChart.fitScreen();
        barChart.calculateOffsets();
        barChart.invalidate(); // refresh

        progressBar.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        setTitle(poll.getTitle());
    }

    @Override
    public String getTitle() {
        return "Poll";
    }

    @Override
    public boolean volumeScroll(KeyEvent event) {
        return false;
    }
}
