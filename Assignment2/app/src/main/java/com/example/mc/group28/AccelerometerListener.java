package com.example.mc.group28;

/**
 * Created by janit on 3/2/18.
 */

public interface AccelerometerListener {
    public void onAccelerationChanged(float x, float y, float z);

    public void onShake(float force);
}
