package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
public class FragmentChat extends Fragment implements CustomAdapter.OnItemClickListener
{
    FirebaseAuth auth = FirebaseAuth.getInstance();

    View chat;

    RecyclerView chatList;

    private CustomAdapter adapter;

    private List<DataItem> dataList;

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

               for(DataSnapshot users: snapshot.child("Users").getChildren())
               {
                   String userKey = users.getKey();

                   if(userId.equals(userKey))
                   {
                       for(DataSnapshot status: users.child("Responders").getChildren())
                       {
                           Boolean checkStatus = status.child("Status").getValue(Boolean.class);
                           String fighterKey = status.getKey();

                           if(checkStatus == true)
                           {
                               String Name = snapshot.child("Firefighter").child(fighterKey).child("lastName").getValue(String.class);
                               String time = status.child("Time").getValue(String.class);
                               String message = "I am on my way!";

                               dataList.add(new DataItem(Name, message, time, fighterKey));

                               adapter.notifyDataSetChanged();
                           }
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

        adapter = new CustomAdapter(dataList, this);

        chatList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(DataItem item)
    {
        String name = item.getName();

        FragmentMessage message = new FragmentMessage();

        Bundle bundle = new Bundle();

        bundle.putString("Name", name);

        message.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();

        fragmentManager.popBackStack();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        transaction
                .replace(R.id.container, message, "fragment_message")
                .addToBackStack(null)
                .commit();

    }
}
