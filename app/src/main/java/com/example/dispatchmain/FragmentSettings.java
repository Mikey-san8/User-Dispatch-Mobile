package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class FragmentSettings extends Fragment {

    View settings;

    public FragmentSettings()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        settings = inflater.inflate(R.layout.fragment_directories, container, false);


        return settings;
    }
}
