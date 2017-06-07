package eu.mhutti1.chat;

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
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.microsoft.windowsazure.mobileservices.MobileServiceActivityResult;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private MobileServiceTable<TodoItem> mToDoTable;

  private MobileServiceClient mClient;
  public static final String MESSAGE_USERNAME = "eu.mhutti1.chat.MESSAGE_USERNAME";
  List<String> conversations = new ArrayList<>();
  ArrayAdapter<String> conversationAdapter;

  public static final int MICROSOFT_LOGIN_REQUEST_CODE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    try {
      mClient = new MobileServiceClient(
          "https://chat-mhutti1.azurewebsites.net",
          this
      );
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }


    if (Utils.myId(this).equals("----")) {
      authenticate();
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

     // new LoadPeople().execute();
    }
  }

  public void loadTodo() {


    TodoItem item = new TodoItem();
    item.Text = "Awesome item";
    ListenableFuture<TodoItem> future = mClient.getTable(TodoItem.class).insert(item);
    Futures.addCallback(future, new FutureCallback<TodoItem>() {
      @Override
      public void onSuccess(TodoItem result) {
        Toast toast = Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT);
        toast.show();
      }

      @Override
      public void onFailure(Throwable t) {
        Toast toast = Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_SHORT);
        toast.show();
      }
    });
  }

  public class TodoItem {
    public String Id;
    public String Text;
  }


  private void authenticate() {
    // Login using the Microsoft provider.
    mClient.login("MicrosoftAccount", "chat", MICROSOFT_LOGIN_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // When request completes
    if (resultCode == RESULT_OK) {
      // Check the request code matches the one we send in the login request
      if (requestCode == MICROSOFT_LOGIN_REQUEST_CODE) {
        MobileServiceActivityResult result = mClient.onActivityResult(data);
        String text = "";
        if (result.isLoggedIn()) {
          // login succeeded
          text = String.format("You are now logged in - %1$2s", mClient.getCurrentUser().getUserId());
          loadTodo();
        } else {
          // login failed, check the error message
          text = result.getErrorMessage();
        }
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
      }
    }
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
}
