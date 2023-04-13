package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecordFragment extends Fragment {

    View record;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    ArrayList<String> recordArray;

    ArrayAdapter<String> recordAdapter;

    ListView recordList;

    public RecordFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        record = inflater.inflate(R.layout.activity_records, container, false);



        events();

        return record;
    }

    public void events()
    {
        recordList = record.findViewById(R.id.listRecord);

        recordArray = new ArrayList<>();

        recordAdapter = new ArrayAdapter<>(getContext(), R.layout.record_list, R.id.recordList, recordArray);

        recordList.setAdapter(recordAdapter);

        getRecord();
    }

    public void getRecord()
    {
        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firebaseFirestore.collection("Users");

        DocumentReference userRef = firebaseFirestore.collection("Users").document(userId);
        CollectionReference reportRef = userRef.collection("Reports");

        Map <String, Object> record = new HashMap<>();

        reportRef.get().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                for (QueryDocumentSnapshot document : task.getResult())
                {
                    record.putAll(document.getData());
                    String location = String.valueOf(record.get("Location"));
                    String timeDate = String.valueOf(record.get("Date & Time"));
                    recordArray.add("Location: " + location + "\nDate & Time: " + timeDate);
                    recordAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
