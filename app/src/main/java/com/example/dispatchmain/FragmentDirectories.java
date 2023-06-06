package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class FragmentDirectories extends Fragment {

    View directory;

    public FragmentDirectories()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        directory = inflater.inflate(R.layout.fragment_directories, container, false);

        directory.findViewById(R.id.backToHome).setOnClickListener(new View.OnClickListener() {
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
        return directory;
    }
}
