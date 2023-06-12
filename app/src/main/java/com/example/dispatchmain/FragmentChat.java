package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class FragmentChat extends Fragment implements xChatAdapter.OnItemClickListener
{
    FirebaseAuth auth = FirebaseAuth.getInstance();

    View chat;

    RecyclerView chatList;

    private xChatAdapter adapter;

    private List<zDataItem> dataList;

    public FragmentChat()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        chat = inflater.inflate(R.layout.fragment_chat, container, false);

        changeList();

        return chat;
    }

    DataSnapshot lastMessage;
    String key;
    String name;
    String time;
    String userName;
    Boolean getAnonymous;

    public void retrieveStatus()
    {
        String userId = auth.getCurrentUser().getUid();

        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!dataList.isEmpty())
                {
                    dataList.clear();
                }

                if(snapshot.child("Messages").hasChild("Firefighter"))
                {
                    for(DataSnapshot messages: snapshot.child("Messages").child("Firefighter").getChildren())
                    {
                        String getUser = messages.getKey();

                        for (DataSnapshot user : messages.child(userId).getChildren())
                        {
                            name            = snapshot.child("Firefighter").child(getUser).child("lastName").getValue(String.class);
                            time            = user.child("time").getValue(String.class);
                            userName        = snapshot.child("Firefighter").child(getUser).child("userId").getValue(String.class);
                            getAnonymous    = snapshot.child("Firefighter").child(getUser).child("Settings").child("send anonymous").getValue(Boolean.class);
                            lastMessage     = user;
                        }

                        String updatedUserName = null;

                        if (userName != null && userName.length() > 20)
                        {
                            updatedUserName = "fighter." + userName.substring(0, userName.length() - 20);
                        }

                        String getName  = null;
                        String getEmail = null;

                        if(getAnonymous == true)
                        {
                            getName     = updatedUserName;
                        }
                        else
                        {
                            getName     = name;
                        }

                        if (lastMessage != null)
                        {
                            Object messageValue = lastMessage.child("message").getValue();
                            String getMessage = null;

                            if (messageValue instanceof Double) {
                                Double doubleValue = (Double) messageValue;
                                getMessage = String.valueOf(doubleValue);
                            }
                            else if (messageValue instanceof String)
                            {
                                String stringValue = (String) messageValue;
                                getMessage = stringValue;
                            }
                            else if (messageValue instanceof Integer)
                            {
                                Integer integerValue = (Integer) messageValue;
                                getMessage = String.valueOf(integerValue);
                            }

                            dataList.add(new zDataItem(getName, getMessage, time, getUser));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        };

        databaseReference.addValueEventListener(valueEventListener);
        databaseReference.keepSynced(true);
    }

    public void changeList()
    {
        retrieveStatus();

        chatList = chat.findViewById(R.id.listChat);

        chatList.setLayoutManager(new LinearLayoutManager(requireContext()));

        dataList = new ArrayList<>();

        adapter = new xChatAdapter(dataList, this);

        chatList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(zDataItem item)
    {
        String name = item.getName();

        FragmentMessage message = new FragmentMessage();

        Bundle bundle = new Bundle();

        bundle.putString("Name", name);
        bundle.putString("Key", item.getKey());

        message.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        transaction
                .replace(R.id.container, message, "fragment_message")
                .addToBackStack(null)
                .commit();

    }
}
