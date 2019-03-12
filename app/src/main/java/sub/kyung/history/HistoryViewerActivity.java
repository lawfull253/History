package sub.kyung.history;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryViewerActivity extends AppCompatActivity {
    private static final String DATE_FORMAT = "yyyy년 MM월 dd일";

    private TextView dateDisplayTextView;
    private TextView keywordTextView;
    private TextView contentTextView;
    private ImageButton closeButton;

    private Intent data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_viewer);

        initialized();
        assignOnClickListener();
        setView();
    }

    private void initialized(){
        dateDisplayTextView = findViewById(R.id.viewer_date_display_textview);
        keywordTextView = findViewById(R.id.viewer_keyword_textview);
        contentTextView = findViewById(R.id.viewer_content_textview);
        closeButton = findViewById(R.id.viewer_close_button);

        data = getIntent();
    }

    private void assignOnClickListener(){
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setView(){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        dateDisplayTextView.setText(sdf.format(new Date(data.getStringExtra("date"))));
        keywordTextView.setText(data.getStringExtra("keyword"));
        contentTextView.setText(data.getStringExtra("content"));
    }
}
