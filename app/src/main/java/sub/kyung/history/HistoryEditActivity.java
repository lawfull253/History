package sub.kyung.history;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryEditActivity extends AppCompatActivity {
    private static final int CREATE_MODE = 100;
    private static final int EDIT_MODE = 101;
    private static final String DATE_FORMAT = "yyyy년 MM월 dd일";

    private TextView dateDisplayTextView;
    private EditText keywordEditText;
    private EditText contentEditText;
    private Button closeButton;
    private Button okayButton;

    private Database db;

    private Intent data;

    private History history;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_edit);

        initialized();
        setDataAndSetView();
        assignOnClickListener();
    }

    private void initialized(){
        this.dateDisplayTextView = findViewById(R.id.edit_date_display_textview);
        this.keywordEditText = findViewById(R.id.edit_keyword_edittext);
        this.contentEditText = findViewById(R.id.edit_content_edittext);
        this.closeButton = findViewById(R.id.edit_close_button);
        this.okayButton = findViewById(R.id.edit_okay_button);

        this.db = new Database(this);
        this.data = getIntent();
    }

    private void setDataAndSetView(){
        this.mode = data.getIntExtra("mode", 0);
        history = new History(null, null, new Date(data.getStringExtra("date")));

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        dateDisplayTextView.setText(sdf.format(history.getHistoryDate()));

        if(this.mode == EDIT_MODE){
            history.setHistoryKeyword(data.getStringExtra("keyword"));
            history.setHistoryContent(data.getStringExtra("content"));

            keywordEditText.setText(history.getHistoryKeyword());
            contentEditText.setText(history.getHistoryContent());
        }
    }

    private void assignOnClickListener(){
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                history.setHistoryKeyword(keywordEditText.getText().toString());
                history.setHistoryContent(contentEditText.getText().toString());

                if(mode == CREATE_MODE){
                    db.insertHistory(history);
                } else if(mode == EDIT_MODE){
                    db.updateHistory(history);
                }

                data.putExtra("keyword", history.getHistoryKeyword());
                data.putExtra("content", history.getHistoryContent());

                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
