package test.annotation.serial.bean;

import java.io.Serializable;

import tools.android.serial.annotation.Serial;

@Serial
public class Content implements Serializable {
    int identify;
    String author_id;
    boolean crypt;
    long duration;
    byte size;
    short offset;
    char point;
    float dot;
    double time;

    Long vd;
}
