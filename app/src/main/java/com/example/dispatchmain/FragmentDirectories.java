package com.example.dispatchmain;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class FragmentDirectories extends Fragment implements View.OnClickListener
{
    private FusedLocationProviderClient fusedLocationClient;

    View directory;

    CardView    cardDetails,
                cardMessage,
                cardCall,
                cardAlert;

    TextView    details, tapItem;

    ListView    listDirectory;

    ArrayList<String>       arrayDirectory;

    ArrayAdapter<String>    adapterDirectory;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    List<String> setDetail      = new ArrayList<>();
    List<String> mobileNumbers  = new ArrayList<>();
    List<String> getListName    = new ArrayList<>();

    Map<String, Object> getKey = new HashMap<>();

    FragmentMessage fragmentMessage = new FragmentMessage();

    ImageView infoDirectory;

    Drawable currentDrawable;

    Bitmap currentBitmap;

    int getPosition;

    zCalculations calculate;

    public FragmentDirectories()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        directory = inflater.inflate(R.layout.fragment_directories, container, false);

        events();
        getAvailable();

        directory.findViewById(R.id.backDirectory).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentHome home = new FragmentHome();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                transaction
                        .replace(R.id.container, home, "fragment_home")
                        .addToBackStack(null)
                        .commit();
            }
        });

        listDirectory.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                cardDetails         .setVisibility(View.VISIBLE);
                cardMessage         .setVisibility(View.VISIBLE);
                cardCall            .setVisibility(View.VISIBLE);
                tapItem             .setVisibility(View.INVISIBLE);

                getPosition = position;

                details.setText(setDetail.get(position));
            }
        });

        return directory;
    }

    private void events()
    {
        calculate = new zCalculations(directory.getContext());

        cardDetails         = directory.findViewById(R.id.cardDirectory);
        cardMessage         = directory.findViewById(R.id.cardMessage);
        cardCall            = directory.findViewById(R.id.cardCall);
        cardAlert           = directory.findViewById(R.id.cardAlert);

        cardDetails         .setVisibility(View.INVISIBLE);
        cardMessage         .setVisibility(View.INVISIBLE);
        cardCall            .setVisibility(View.INVISIBLE);
        cardAlert           .setVisibility(View.INVISIBLE);

        infoDirectory       = directory.findViewById(R.id.infoDirectory);

        currentDrawable     = infoDirectory.getDrawable();

        currentBitmap       = drawableToBitmap(currentDrawable);

        details             = directory.findViewById(R.id.detailDirectory);
        tapItem             = directory.findViewById(R.id.textTapDirectory);

        listDirectory       = directory.findViewById(R.id.listDirectory);

        arrayDirectory      = new ArrayList<>();

        adapterDirectory    = new ArrayAdapter<>(getContext(), R.layout.list_directory, R.id.itemDirectory, arrayDirectory);

        listDirectory       .setAdapter(adapterDirectory);

        directory       .findViewById(R.id.closeDirectory)
                        .setOnClickListener(this);
        infoDirectory   .setOnClickListener(this);
        cardCall        .setOnClickListener(this);
        cardMessage     .setOnClickListener(this);
    }

    private void getAvailable()
    {
        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!arrayDirectory.isEmpty())
                {
                    arrayDirectory      .clear();
                    adapterDirectory    .notifyDataSetChanged();
                    setDetail           .clear();
                    mobileNumbers       .clear();
                    getListName         .clear();
                    getKey              .clear();
                }

                fusedLocationClient.getLastLocation().addOnCompleteListener(task ->
                {
                    if(task.isSuccessful())
                    {
                        Location location = task.getResult();

                        for (DataSnapshot Snapshot: snapshot.child("Firefighter").getChildren())
                        {
                            String key = Snapshot.getKey();

                            if(Snapshot.hasChild("Settings"))
                            {
                                Boolean onlineStatus    = Snapshot.child("Settings").child("online status").getValue(Boolean.class);
                                Boolean getAnonymous    = Snapshot.child("Settings").child("send anonymous").getValue(Boolean.class);

                                if(onlineStatus == true)
                                {
                                    Double lat = Snapshot.child("lat").getValue(Double.class);
                                    Double lng = Snapshot.child("lng").getValue(Double.class);

                                    LatLng fighterLocation = new LatLng(lat, lng);
                                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                    if(calculate.calculateDistance(fighterLocation, userLocation) < 7001)
                                    {
                                        String userName = Snapshot.child("userId")      .getValue(String.class);
                                        String updatedUserName = null;

                                        if (userName != null && userName.length() > 20)
                                        {
                                            updatedUserName = "fighter." + userName.substring(0, userName.length() - 20);
                                        }

                                        String name     = Snapshot.child("lastName")    .getValue(String.class);
                                        String phone    = Snapshot.child("phone")       .getValue(String.class);
                                        String address  = Snapshot.child("station")     .getValue(String.class);
                                        String email    = Snapshot.child("email")       .getValue(String.class);

                                        double getDistance = calculate.calculateDistance(fighterLocation, userLocation) / 1000;

                                        DecimalFormat decimalFormat = new DecimalFormat("#.##");

                                        String formattedDistance = decimalFormat.format(getDistance);

                                        String getName  = null;
                                        String getEmail = null;

                                        if(getAnonymous == true)
                                        {
                                            getName     = updatedUserName;

                                            if (email != null)
                                            {
                                                int atIndex = email.indexOf("@");

                                                String firstCharacter = email.substring(0, 1);
                                                String asterisks = "*".repeat(atIndex - 1);
                                                String maskedEmail = firstCharacter + asterisks + email.substring(atIndex);

                                                getEmail = maskedEmail;
                                            }
                                        }
                                        else
                                        {
                                            getName     = name;
                                            getEmail    = email;
                                        }

                                        arrayDirectory.add("Name:       "+getName+
                                                "\nDistance:    "+formattedDistance+ "km");
                                        adapterDirectory.notifyDataSetChanged();

                                        setDetail.add("Name:    "+getName+
                                                "\nMobile number:   "+phone+
                                                "\nStation Name/Adress: "+address+
                                                "\nEmail:   "+getEmail);

                                        mobileNumbers   .add(phone);
                                        getListName     .add(getName);
                                        getKey.put(getName, key);
                                    }
                                }
                            }
                        }

                        for(DataSnapshot Snapshot: snapshot.child("Users").getChildren())
                        {
                            String checkID = Snapshot.getKey();

                            if(userId == checkID)
                            {
                                long currentTime = System.currentTimeMillis();

                                long thirtyMinutesInMillis = 30 * 60 * 1000;

                                if(Snapshot.child("Report").hasChild("TimeStamp"))
                                {
                                    long timeStamp = Snapshot.child("Report").child("TimeStamp").getValue(Long.class);

                                    if (currentTime - timeStamp >= thirtyMinutesInMillis)
                                    {
                                        firebaseDatabase.getReference().child("Users").child(userId).child("Report").child("Location").removeValue();
                                    }
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        };

        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.cardCall:

                String getPhone = mobileNumbers.get(getPosition);

                AlertDialog.Builder builder = new AlertDialog.Builder(directory.getContext());
                builder.setMessage("Proceed to call? "+getPhone);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + getPhone));
                        startActivity(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                break;
            case R.id.cardMessage:

                Bundle bundle = new Bundle();

                String name     = getListName.get(getPosition);
                String key      = String.valueOf(getKey.get(name));

                bundle.putString("Key", key);
                bundle.putString("Name", name);

                fragmentMessage.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentMessage, "message_fragment")
                        .addToBackStack(null)
                        .commit();

                break;
            case R.id.closeDirectory:

                cardDetails         .setVisibility(View.INVISIBLE);
                cardMessage         .setVisibility(View.INVISIBLE);
                cardCall            .setVisibility(View.INVISIBLE);

                tapItem             .setVisibility(View.VISIBLE);

                break;
            case R.id.infoDirectory:

                if(cardAlert.getVisibility() == View.INVISIBLE)
                {
                    cardAlert    .setVisibility(View.VISIBLE);

                    Drawable newDrawable =
                            getResources().getDrawable(R.drawable.arrow_right);

                    infoDirectory.setImageDrawable(newDrawable);
                }

                else
                {
                    cardAlert    .setVisibility(View.INVISIBLE);

                    Bitmap newBitmap = currentBitmap;

                    infoDirectory.setImageBitmap(newBitmap);
                }
                break;
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
