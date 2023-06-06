package com.example.dispatchmain;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("ALL")
public class FragmentRecord extends Fragment implements View.OnClickListener {

    View record;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    ArrayList<String> recordArray;

    ArrayAdapter<String> recordAdapter;

    ListView recordList;

    public FragmentRecord()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        record = inflater.inflate(R.layout.fragment_record, container, false);



        events();

        return record;
    }

    public void events()
    {
        recordList = record.findViewById(R.id.listRecord);

        recordArray = new ArrayList<>();

        recordAdapter = new ArrayAdapter<>(getContext(), R.layout.list_record, R.id.recordList, recordArray);

        recordList.setAdapter(recordAdapter);

        record.findViewById(R.id.deleteHistory).setOnClickListener(this);

        getRecord();
    }

    public void getRecord()
    {
        String userId = auth.getCurrentUser().getUid();

        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference("Users");

        databaseReference.child(userId).child("Report").child("Reports").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot Snapshot: snapshot.getChildren())
                {
                    String location = "Location: " +
                            TextUtils.ellipsize(Snapshot.child("Location")
                                            .getValue(String.class),
                            new TextPaint(), 250, TextUtils.TruncateAt.END).toString();

                    String timeDate = "Date & Time: " +
                            Snapshot.child("Date & Time")
                                    .getValue(String.class);

                    recordArray.add(location + "\n" + timeDate);
                    recordAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
        databaseReference.keepSynced(true);
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.deleteHistory)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("Are you sure you want to delete?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                    FirebaseDatabase firebaseDatabase;
                    DatabaseReference databaseReference;
                    firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
                    databaseReference = firebaseDatabase.getReference("Users");

                    databaseReference.child(userID).child("Report").child("Reports").removeValue(new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref)
                        {
                            if (error != null)
                            {
                                Toast.makeText(requireContext(), "Error Deleting...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                FragmentRecord fragment = new FragmentRecord();
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.container, fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                        }
                    });
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
