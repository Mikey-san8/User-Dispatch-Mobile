package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class FragmentMessage extends Fragment
{
    View message;

    TextView chatName;

    private List<DataItem> dataList;

    public FragmentMessage()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        message = inflater.inflate(R.layout.fragment_message, container, false);

        chatName = message.findViewById(R.id.chatName);

        message.findViewById(R.id.backToChat).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();

                FragmentChat chat = new FragmentChat();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                transaction
                        .replace(R.id.container, chat, "fragment_chat")
                        .addToBackStack(null)
                        .commit();
            }
        });

        String name = getArguments().getString("Name");
        chatName.setText(name);

        return message;
    }
}
