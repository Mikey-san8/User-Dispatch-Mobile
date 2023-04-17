package com.example.dispatchmain;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RecordFragment extends Fragment implements View.OnClickListener {

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

        record.findViewById(R.id.deleteHistory).setOnClickListener(this);

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

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.deleteHistory) {
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

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference collectionRef = db.collection("Users");

                    DocumentReference documentReference = collectionRef.document(userID);
                    CollectionReference collectionReference = documentReference.collection("Reports");

                    collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                for (QueryDocumentSnapshot document : task.getResult())
                                {
                                    document.getReference().delete();
                                }
                                Toast.makeText(requireActivity(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                RecordFragment fragment = new RecordFragment();
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.container, fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                            else
                            {
                                Toast.makeText(requireActivity(), "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
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
