package eu.mhutti1.chat;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class ConversationActivity extends AppCompatActivity {

  final List<Message> conversation = new ArrayList<>();
  ChatAdapter conversationAdapter;
  String username;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_conversation);
    String username = getIntent().getStringExtra(MainActivity.MESSAGE_NICK);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(username);
    this.username = username;


    ListView conversationListView = (ListView) findViewById(R.id.conversation);
    conversationAdapter = new ChatAdapter(this, android.R.layout.simple_list_item_1, conversation);
    conversationListView.setAdapter(conversationAdapter);
    TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.message_box);
    textInputLayout.getEditText().setOnEditorActionListener(new EditText.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEND) {
          conversation.add(new Message(Utils.myId(getApplicationContext()), "", textView.getText().toString()));
          conversationAdapter.notifyDataSetChanged();
         // new PostMessage().execute(textView.getText().toString());
          textView.setText("");
          return true;
        }
        return false;
      }
    });
    //new LoadMessages().execute();

  }


}
