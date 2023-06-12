package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
public class FragmentMessage extends Fragment
{
    FirebaseAuth auth = FirebaseAuth.getInstance();

    View message;

    TextView chatName;

    private xMessageAdapter adapter;

    private List<zDataMessage> dataList;

    FirebaseDatabase firebaseDatabase;

    DatabaseReference databaseReference;

    RecyclerView chatMessage;

    EditText        typeMessage;
    TextInputLayout inputMessage;

    public FragmentMessage()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        message = inflater.inflate(R.layout.fragment_message, container, false);

        eventMessages();

        message.findViewById(R.id.backToChat).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();

//                FragmentChat chat = new FragmentChat();
//
//                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//
//                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
//
//                transaction
//                        .replace(R.id.container, chat, "fragment_chat")
//                        .addToBackStack(null)
//                        .commit();
            }
        });

        return message;
    }

    private void eventMessages()
    {
        receiveMessage();

        typeMessage         = message.findViewById(R.id.typeMessage);
        inputMessage        = message.findViewById(R.id.inputMessage);
        chatName            = message.findViewById(R.id.chatName);
        String name         = getArguments().getString("Name");
        chatName            .setText(name);
        chatMessage         = message.findViewById(R.id.listMessage);
        chatMessage         .setLayoutManager(new LinearLayoutManager(requireContext()));
        dataList            = new ArrayList<>();
        adapter             = new xMessageAdapter(dataList);
        chatMessage         .setAdapter(adapter);

        inputMessage.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    String name;
    String getKey;

    public void receiveMessage()
    {
        String key = getArguments().getString("Key");

        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!dataList.isEmpty())
                {
                    dataList.clear();
                }

                if(snapshot.hasChild("Messages"))
                {
                    for(DataSnapshot messages: snapshot.child("Messages").child("Firefighter").getChildren())
                    {
                        String getUser = messages.getKey();

                        if(getUser.equals(key))
                        {
                            for (DataSnapshot user : messages.child(userId).getChildren())
                            {
                                String userID = user.getKey();

                                String name = snapshot.child("Firefighter").child(key).child("lastName").getValue(String.class);
                                String time = user.child("time").getValue(String.class);
                                String message = user.child("message").getValue(String.class);

                                dataList.add(new zDataMessage(name, message, time, key, "fighter"));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    if(snapshot.child("Messages").hasChild("User"))
                    {
                        for(DataSnapshot messages: snapshot.child("Messages").child("User").getChildren())
                        {
                            String getUser = messages.getKey();

                            if(getUser.equals(userId))
                            {
                                for (DataSnapshot user : messages.child(key).getChildren())
                                {
                                    String userID = user.getKey();

                                    String name = snapshot.child("Users").child(userId).child("lastName").getValue(String.class);
                                    String time = user.child("time").getValue(String.class);
                                    String message = user.child("message").getValue(String.class);

                                    dataList.add(new zDataMessage(name, message, time, userId, "user"));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }

                if (dataList.size() > 0)
                {
                    int lastItemPosition = dataList.size() - 1;

                    chatMessage.scrollToPosition(lastItemPosition);
                }

                adapter.sortMessagesByTime();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void allMessages()
    {
        String key = getArguments().getString("Key");
        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

    }
    private void sendMessage()
    {
        String key = getArguments().getString("Key");
        String userId = auth.getCurrentUser().getUid();

        String getTyped = typeMessage.getText().toString();

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeDate = dateFormat.format(new Date());

        Map<String, Object> sendMessage = new HashMap<>();
        sendMessage.put("message", getTyped);
        sendMessage.put("time", timeDate);
        sendMessage.put("name", name);

        databaseReference.child("Messages").child("User").child(userId).child(key).push().updateChildren(sendMessage);
        dataList.add(new zDataMessage(name, getTyped, timeDate, getKey, "user"));
        adapter.notifyDataSetChanged();

        typeMessage.setText("");

        if (dataList.size() > 0)
        {
            int lastItemPosition = dataList.size() - 1;
            chatMessage.scrollToPosition(lastItemPosition);
        }
    }
}
