package com.example.xaimy.locator;

/**
 * Created by Xaimy on 10/29/2017.
 */

public class TimeStamp
{
    public String Longitude,Latitude;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public TimeStamp()
    {
    }

    public TimeStamp(String Longitude, String Latitude)
    {
        this.Longitude = Longitude;
        this.Latitude = Latitude;
    }
}
