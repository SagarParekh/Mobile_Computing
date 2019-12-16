package com.example.mc.group28;

public class Patient {
        private float x;
        private float y;
        private float z;
    private long time_stamp;

        public Patient(float x, float y, float z, long time_stamp) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time_stamp = time_stamp;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getZ() {
            return z;
        }

        public void setZ(float z) {
            this.z = z;
        }

        public long getTime_stamp() {
            return time_stamp;
        }

        public void setTime_stamp(long time_stamp) {
            this.time_stamp = time_stamp;
        }


}
