package com.example.catchcontroller;

import android.app.Application;

import java.util.ArrayList;

public class MyApplication extends Application {

    public ArrayList<String> waypoints;

    public MyApplication() {
        waypoints = new ArrayList<>();
    }

    public ArrayList<String> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(ArrayList<String> waypoints) {
        this.waypoints= waypoints;
    }

    public void addWaypoint(String waypoint) {
        this.waypoints.add(waypoint);
    }

    public void removeWaypoint(String waypoint) {
        this.waypoints.remove(waypoint);
    }

    public void clearWaypoints() {
        this.waypoints.clear();
    }
}