package test.annotation.serial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import test.annotation.serial.bean.ContentSerial;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentSerial content = new ContentSerial();
                content.setIdentify(1920);
                int v = content.getIdentify();
                content.setDuration(3000L);
                long l = content.getDuration();

            }
        });
    }
}
