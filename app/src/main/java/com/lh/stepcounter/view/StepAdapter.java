package com.lh.stepcounter.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lh.stepcounter.R;
import com.lh.stepcounter.bean.StepInfo;

import java.util.List;
import java.util.Locale;

/**
 * Created by home on 2017/1/12.
 */

public class StepAdapter extends RecyclerView.Adapter {

    private List<StepInfo> stepInfos;

    public StepAdapter(List<StepInfo> stepInfos) {
        this.stepInfos = stepInfos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.step_item_view,parent,false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StepViewHolder stepViewHolder = (StepViewHolder) holder;
        StepInfo info = stepInfos.get(position);
        stepViewHolder.mTimeView.setText(info.getDate());
        stepViewHolder.mStepCountView.setText(String.format(Locale.getDefault(),"%d步",info.getStep()));
    }

    @Override
    public int getItemCount() {
        return stepInfos.size();
    }

    private static class StepViewHolder extends RecyclerView.ViewHolder{

        private TextView mTimeView;
        private TextView mStepCountView;

        StepViewHolder(View itemView) {
            super(itemView);
            mTimeView = (TextView) itemView.findViewById(R.id.time);
            mStepCountView = (TextView) itemView.findViewById(R.id.step_count);
        }
    }
}
