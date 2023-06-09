package com.example.dispatchmain;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class FragmentCheckProfile extends Fragment implements View.OnClickListener {

    View check;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    TextView name, phone, email, location;

    LinearLayout linearPassword;

    EditText currentPassword, newPassword, confirmPassword;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    TextView showPass;

    RelativeLayout checkProfile;

    Button editProfile;

    public FragmentCheckProfile()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        check = inflater.inflate(R.layout.fragment_checkprofile, container, false);

        events();
        retrieveDetails();

        return check;
    }

    public void events()
    {
        name = check.findViewById(R.id.checkName);
        phone = check.findViewById(R.id.checkMobile);
        email = check.findViewById(R.id.checkEmail);
        location = check.findViewById(R.id.checkAddress);

        linearPassword = check.findViewById(R.id.linearPassword);
        linearPassword.setVisibility(View.INVISIBLE);

        currentPassword = check.findViewById(R.id.inputCurrent);
        newPassword = check.findViewById(R.id.inputNew);
        confirmPassword = check.findViewById(R.id.inputConfirm);

        check.findViewById(R.id.checkPassword).setOnClickListener(this);
        check.findViewById(R.id.confirmChange).setOnClickListener(this);
        check.findViewById(R.id.checkBack).setOnClickListener(this);
        check.findViewById(R.id.closeChange).setOnClickListener(this);

        editProfile = check.findViewById(R.id.editCheckProfile);
        editProfile.setOnClickListener(this);

        showPass = check.findViewById(R.id.showPassword);
        showPass.setOnClickListener(this);

        checkProfile = check.findViewById(R.id.layoutCheckProfile);

        checkProfile.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                View view = getActivity().getCurrentFocus();

                if (view != null)
                {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm != null)
                    {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

                return false;
            }
        });
    }

    public void retrieveDetails()
    {
        ValueEventListener valueEventListener;

        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                String dfirstName =  snapshot.child("Users").child(userId).child("firstName").getValue(String.class);
                String dlastName = snapshot.child("Users").child(userId).child("lastName").getValue(String.class);
                String duserName = snapshot.child("Users").child(userId).child("userName").getValue(String.class);
                String demail =  snapshot.child("Users").child(userId).child("email").getValue(String.class);
                String dphone = snapshot.child("Users").child(userId).child("phone").getValue(String.class);
                String daddress = snapshot.child("Users").child(userId).child("address").getValue(String.class);

                name.setText(dfirstName+" "+dlastName);
                phone.setText(dphone);
                email.setText(demail);
                location.setText(daddress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        databaseReference.keepSynced(true);
    }

    @Override
    public void onClick(View v)
    {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        switch (v.getId())
        {
            case R.id.checkPassword:
                linearPassword.setVisibility(View.VISIBLE);
                break;
            case R.id.confirmChange:
                String userId = auth.getCurrentUser().getUid();
                String email = auth.getCurrentUser().getEmail();
                String password = currentPassword.getText().toString();
                String newPass = newPassword.getText().toString();
                String confirmPass = confirmPassword.getText().toString();

                Map<String, Object> dataPassword = new HashMap<>();

                firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
                databaseReference = firebaseDatabase.getReference();

                AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                auth.getCurrentUser().reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    if(newPass.equals(confirmPass) && isPasswordValid(newPass) == true && newPass != password)
                                    {
                                        auth.getCurrentUser().updatePassword(newPass)
                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            dataPassword.put("password", newPass);

                                                            Toast.makeText(requireContext(), "Password Updated", Toast.LENGTH_SHORT).show();

                                                            databaseReference.child("Users").child(userId).updateChildren(dataPassword);

                                                            currentPassword.setText("");
                                                            newPassword.setText("");
                                                            confirmPassword.setText("");

                                                            linearPassword.setVisibility(View.INVISIBLE);
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(requireContext(), "Not Updated", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                    else
                                    {
                                        if(!newPass.equals(confirmPass))
                                        {
                                            confirmPassword.setTextColor(Color.RED);
                                            Toast.makeText(requireContext(), "Check Confirm Password", Toast.LENGTH_SHORT).show();

                                            Toast.makeText(requireContext(), newPass+" "+confirmPass, Toast.LENGTH_SHORT).show();
                                        }
                                        if(isPasswordValid(newPass) == false)
                                        {
                                            Toast.makeText(requireContext(), "New Password Invalid", Toast.LENGTH_SHORT).show();
                                        }
                                        if(newPass == password)
                                        {
                                            Toast.makeText(requireContext(), "Don't use current password", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }
                                else
                                {
                                    currentPassword.setTextColor(Color.RED);
                                    Toast.makeText(requireContext(), "Check Current Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                break;
            case R.id.editCheckProfile:

                FragmentEditProfile fragmentEditProfile = new FragmentEditProfile();

                transaction
                        .replace(R.id.container, fragmentEditProfile, "edit_fragment")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.checkBack:
                FragmentHome fragmentHome = (FragmentHome) getParentFragmentManager().findFragmentByTag("home_fragment");

                transaction
                        .replace(R.id.container, fragmentHome)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.showPassword:

                if(showPass.getText().toString() == "Hide Password")
                {
                    currentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    showPass.setText("Show Password");
                }
                else
                {
                    showPass.setText("Hide Password");

                    currentPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                break;
            case R.id.closeChange:
                currentPassword.setText("");
                newPassword.setText("");
                confirmPassword.setText("");
                linearPassword.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public boolean isPasswordValid(String password)
    {
        if (password.length() < 7) {
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }

        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")) {
            return false;
        }

        return true;
    }

}
