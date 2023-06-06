package com.example.dispatchmain;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsStep;

public class zPolylineData {

    private Polyline polyline;
    private DirectionsLeg leg;
    private DirectionsStep steps;
    private String ID;

    public zPolylineData(Polyline polyline, DirectionsLeg leg, DirectionsStep steps, String ID)
    {
        this.polyline = polyline;
        this.leg = leg;
        this.steps = steps;
        this.ID = ID;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public DirectionsLeg getLeg() {
        return leg;
    }

    public void setLeg(DirectionsLeg leg) {
        this.leg = leg;
    }

    public DirectionsStep getSteps() {
        return steps;
    }

    public void setSteps(DirectionsStep steps) {
        this.steps = steps;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    @Override
    public String toString()
    {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}';
    }
}