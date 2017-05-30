package com.example.umansia.beacon.ui.main;

/**
 * Created by Umansia on 19.03.2017.
 */

public class Vector
{
    private final double x;
    private final double y;
    private final double z;

    public Vector(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector plus(Vector other)
    {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    // etc
}
