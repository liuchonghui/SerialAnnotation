package test.annotation.serial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import test.annotation.serial.entity.Ex;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ex content = new Ex();
                content.setDuration(3000L);
                long a = content.getDuration();
                content.setIdentify(1224);
                int b = content.getIdentify();
                content.setAuthorId("vk");
                String c = content.getAuthorId();
                content.setCrypt(false);
                boolean d = content.getCrypt();
                content.setDot(.1f);
                float e = content.getDot();
                content.setOffset((short)1000);
                short f = content.getOffset();
                content.setPoint('g');
                char g = content.getPoint();
                content.setSize((byte)0xdead);
                byte h = content.getSize();
                content.setTime(0.0028d);
                double i = content.getTime();
                content.setVd(new Long(100L));
                Long j = content.getVd();
                Log.d("PPP", a + "|" + b + "|" + c + "|" + d + "|"
                        + e + "|" + f + "|" + g + "|" + h + "|" + i + "|" + j);
            }
        });
    }
}
