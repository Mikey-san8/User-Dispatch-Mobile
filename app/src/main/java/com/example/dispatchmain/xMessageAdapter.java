package com.example.dispatchmain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("ALL")
public class xMessageAdapter extends RecyclerView.Adapter<xMessageAdapter.ViewHolder>
{
    public List<zDataMessage> dataList;

    public xMessageAdapter(List<zDataMessage> dataList)
    {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        zDataMessage item = dataList.get(position);

        holder.textMessage.setText(item.getChat());
        String time = item.getTime();

        if (time.length() > 3)
        {
            time = time.substring(0, time.length() - 3);
        }

        holder.timeMessage.setText(time);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        if ("fighter".equals(item.getSender())) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            timeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        holder.textMessage.setLayoutParams(params);

        // Set margins for timeParams
        int marginStart = 10; // in pixels
        timeParams.addRule(RelativeLayout.BELOW, R.id.textMessage);
        timeParams.setMarginStart(marginStart);

        holder.timeMessage.setLayoutParams(timeParams);
    }


    public void sortMessagesByTime()
    {
        Collections.sort(dataList, new Comparator<zDataMessage>()
        {
            @Override
            public int compare(zDataMessage o1, zDataMessage o2)
            {
                return o1.getTime().compareTo(o2.getTime());
            }
        });

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount()
    {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textMessage;
        TextView timeMessage;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            textMessage     = itemView.findViewById(R.id.textMessage);
            timeMessage     = itemView.findViewById(R.id.timeMessage);
        }
    }
}
