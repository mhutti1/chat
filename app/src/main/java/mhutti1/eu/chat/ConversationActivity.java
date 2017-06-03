package mhutti1.eu.chat;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_conversation);
    String username = getIntent().getStringExtra(MainActivity.MESSAGE_USERNAME);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(username);
    ListView conversationListView = (ListView) findViewById(R.id.conversation);
    final List<String> conversation = new ArrayList<>();
    conversation.add("Hello");
    final ArrayAdapter<String> conversationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, conversation);
    conversationListView.setAdapter(conversationAdapter);
    TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.message_box);
    textInputLayout.getEditText().setOnEditorActionListener(new EditText.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEND) {
          conversation.add(textView.getText().toString());
          conversationAdapter.notifyDataSetChanged();
          textView.setText("");
          return true;
        }
        return false;
      }
    });
  }
}
