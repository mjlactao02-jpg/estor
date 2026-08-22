package estor.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button button2, button3, button4, button5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);


        button2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdddeptActivity.class);
            startActivity(intent);
        });
        button3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PaydeptActivity.class);
            startActivity(intent);
        });
        button4.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
            startActivity(intent);
        });
        button5.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
}