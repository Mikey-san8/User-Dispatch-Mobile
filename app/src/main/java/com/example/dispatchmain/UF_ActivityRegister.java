package com.example.dispatchmain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@SuppressWarnings("ALL")
public class UF_ActivityRegister extends AppCompatActivity{

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    RelativeLayout register_tap_screen;

    private RadioButton tnc;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.uf_register);


        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView exit = findViewById(R.id.ufExit);
        exit.setOnClickListener(view -> switchToLogin());
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null)
        {
            finish();
            return;
        }

        tnc = findViewById(R.id.ufTnC);

        Button btnRegister = findViewById(R.id.ufRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                registerUser();
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView SwitchToLogin = findViewById(R.id.ufBackToLogin);
        SwitchToLogin.setOnClickListener(view -> switchToLogin());

        register_tap_screen = findViewById(R.id.uf_register_tap_screen);

        register_tap_screen.setOnClickListener(view ->
        {
            InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });
    }

    public void registerUser()
    {
        EditText typeFirstName = findViewById(R.id.ufFirstName);
        EditText typeLastName = findViewById(R.id.ufLastName);
        EditText typeRegisterEmail = findViewById(R.id.ufEmailAddress);
        EditText typeRegisterPassword = findViewById(R.id.ufPassword);
        EditText typeConfirmPassword = findViewById(R.id.ufConfirmPassword);
        EditText typePhoneNumber = findViewById(R.id.ufPhone);
        EditText typeStation = findViewById(R.id.ufAddress);
        EditText typeTelephone = findViewById(R.id.ufTelephone);

        String firstName = typeFirstName.getText().toString();
        String lastName = typeLastName.getText().toString();
        String email = typeRegisterEmail.getText().toString();
        String password = typeRegisterPassword.getText().toString();
        String confirm = typeConfirmPassword.getText().toString();
        String phone = typePhoneNumber.getText().toString();
        String station = typeStation.getText().toString();
        String telephone = typeTelephone.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || phone.isEmpty() || station.isEmpty())
        {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show(); }
        else
        {
            if(!isPasswordValid(password))
            {
                TextView textPassword = findViewById(R.id.passwordText);
                textPassword.setTextColor(Color.RED);
                String passInvalid = "Password Invalid";
                Toast.makeText(this, passInvalid, Toast.LENGTH_SHORT).show();
            }
            else if(!password.equals(confirm))
            {
                Toast.makeText(UF_ActivityRegister.this, "Password Unmatched ", Toast.LENGTH_SHORT).show();
                typeRegisterPassword.setTextColor(Color.RED);
                typeConfirmPassword.setTextColor(Color.RED);
            }
            else
            {
                if(tnc.isChecked())
                {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task ->
                    {
                        if(task.isSuccessful())
                        {
                            String userId = auth.getCurrentUser().getUid();

                            String userName = "fighter." + userId;
                            String updatedUserName = userName.substring(0, userName.length() - 18);

                            mDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
                            zFireMan fireMan = new zFireMan(firstName, lastName, email, station, phone, password, userId, updatedUserName, telephone);
                            mDatabase.child("Firefighter").child(userId).setValue(fireMan)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(UF_ActivityRegister.this, "Success", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(UF_ActivityRegister.this, "Registration Failed", Toast.LENGTH_SHORT).show());
                            logoutUser();
                        }
                        else
                        {
                            Toast.makeText(UF_ActivityRegister.this, "Registration Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                else
                {
                    Toast.makeText(UF_ActivityRegister.this, "Read and agree with the terms and conditions", Toast.LENGTH_SHORT).show();
                }
            }}}

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

    private void switchToLogin()
    {
        Intent intent = new Intent(this, U_ActivityLogin.class);
        startActivity(intent);
        finish();
    }

    private void logoutUser()
    {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, U_ActivityLogin.class);
        startActivity(intent);
        finish();
    }
}