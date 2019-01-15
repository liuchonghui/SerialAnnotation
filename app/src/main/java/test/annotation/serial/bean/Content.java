package test.annotation.serial.bean;

import tools.android.serial.annotation.Serial;

@Serial
public class Content {
    int identify;
    String author_id;
    boolean crypt;
    long duration;
}
