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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConversationActivity extends AppCompatActivity {

  OkHttpClient client = new OkHttpClient();
  final List<Message> conversation = new ArrayList<>();
  ChatAdapter conversationAdapter;
  String username;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_conversation);
    String username = getIntent().getStringExtra(MainActivity.MESSAGE_USERNAME);
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
          new PostMessage().execute(textView.getText().toString());
          textView.setText("");
          return true;
        }
        return false;
      }
    });
    new LoadMessages().execute();

  }




  class LoadMessages extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
      try {
        String json = request("http://mhutti1.eu/getmessages.php?r=" + username + "&s=" + Utils.myId(getApplicationContext()));
        Gson gson = new GsonBuilder().create();
        List<Message> messages = gson.fromJson(json, new TypeToken<List<Message>>() {
        }.getType());
        conversation.addAll(messages);

      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    String request(String url) throws IOException {
      Request request = new Request.Builder()
          .url(url)
          .build();

      Response response = client.newCall(request).execute();
      return response.body().string();
    }

    @Override
    protected void onPostExecute(Void unused) {
      conversationAdapter.notifyDataSetChanged();
    }
  }

  class PostMessage extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
      try {
        request("http://mhutti1.eu/postmessage.php?r=" + username + "&s=" + Utils.myId(getApplicationContext()) + "&t=" + strings[0]);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
      conversationAdapter.notifyDataSetChanged();
    }

    void request(String url) throws IOException {
      Request request = new Request.Builder()
          .url(url)
          .build();

      client.newCall(request).execute();
    }
  }
}
