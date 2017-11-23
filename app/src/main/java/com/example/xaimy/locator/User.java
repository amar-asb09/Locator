package com.example.xaimy.locator;


/**
 * Created by Xaimy on 9/7/2017.
 */

public class User
{
    public String email;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String email)
    {
        this.email = email;
    }
}