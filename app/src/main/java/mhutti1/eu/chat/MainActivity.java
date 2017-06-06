package mhutti1.eu.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

  OkHttpClient client = new OkHttpClient();
  public static final String MESSAGE_USERNAME = "eu.mhutti1.chat.MESSAGE_USERNAME";
  List<String> conversations = new ArrayList<>();
  ArrayAdapter<String> conversationAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    if (Utils.myId(this).equals("----")) {
      showRegisterDialog();
    } else {
      ListView listView = (ListView) findViewById(R.id.conversation_list);
      conversationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, conversations);
      listView.setAdapter(conversationAdapter);
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
          openConversation(conversationAdapter.getItem(i));
        }
      });

      new LoadPeople().execute();
    }
  }

  public void showRegisterDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Please enter a username");
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        new RegisterUser().execute(input.getText().toString());
      }
    });

    builder.show();
  }

  private void openConversation(String username) {
    Intent intent = new Intent(this, ConversationActivity.class);
    intent.putExtra(MESSAGE_USERNAME, username);
    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  class LoadPeople extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
      try {
        String json = request("http://mhutti1.eu/getpeople.php?m=" + Utils.myId(getApplicationContext()));
        Gson gson = new GsonBuilder().create();
        List<String> people = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
        conversations.addAll(people);

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

  class RegisterUser extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String... strings) {
      try {
        String result = request("http://mhutti1.eu/register.php?m=" + strings[0]);
        boolean save = result.equals("true");
        if (save) {
          PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("NAME", strings[0]).commit();
        }
        return save;
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
    protected void onPostExecute(Boolean result) {
      finish();
      startActivity(getIntent());
    }
  }
}
