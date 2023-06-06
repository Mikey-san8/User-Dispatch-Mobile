package com.example.dispatchmain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder>
{
    public List<DataItem> dataList;
    public OnItemClickListener listener;

    public interface OnItemClickListener
    {
        void onItemClick(DataItem item);
    }

    public CustomAdapter(List<DataItem> dataList, OnItemClickListener listener)
    {
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        DataItem item = dataList.get(position);

        holder.textNameResponder.setText(item.getName());
        holder.textResponderChat.setText(item.getChat());
        holder.textTimeChat.setText(item.getTime());
    }

    @Override
    public int getItemCount()
    {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView textNameResponder;
        TextView textResponderChat;
        TextView textTimeChat;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textNameResponder = itemView.findViewById(R.id.textNameResponder);
            textResponderChat = itemView.findViewById(R.id.textResponderChat);
            textTimeChat = itemView.findViewById(R.id.textTimeChat);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION)
            {
                DataItem item = dataList.get(position);

                listener.onItemClick(item);
            }
        }
    }
}

