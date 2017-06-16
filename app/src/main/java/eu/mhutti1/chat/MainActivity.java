package eu.mhutti1.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.microsoft.windowsazure.mobileservices.MobileServiceActivityResult;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private MobileServiceTable<TodoItem> mToDoTable;

  public static MobileServiceClient mClient;
  public static final String MESSAGE_AUTH = "eu.mhutti1.chat.MESSAGE_AUTH";
  public static final String MESSAGE_NICK = "eu.mhutti1.chat.MESSAGE_NICK";
  public static final String USERIDPREF = "uid";
  public static final String TOKENPREF = "tkn";
  public static final String NICKNAMEPREF = "nik";
  private String authId;


  List<User> conversations = new ArrayList<>();
  ArrayAdapter<User> conversationAdapter;

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
    ListView listView = (ListView) findViewById(R.id.conversation_list);
    conversationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, conversations);
    listView.setAdapter(conversationAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        openConversation(conversationAdapter.getItem(i));
      }
    });

    authenticate();
  }

  public void loadUsers() {
    ListenableFuture<MobileServiceList<User>> future = mClient.getTable(User.class).select().execute();
    Futures.addCallback(future, new FutureCallback<MobileServiceList<User>>() {
      @Override
      public void onSuccess(MobileServiceList<User> result) {
        for (User user : result) {
          if (!user.authID.equals(authId)) {
            conversations.add(user);
          }
        }
        conversationAdapter.notifyDataSetChanged();
      }

      @Override
      public void onFailure(Throwable t) {
        Toast toast = Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_SHORT);
        toast.show();
      }
    });
  }

  public void lookUpUser() {
    ListenableFuture<MobileServiceList<User>> future = mClient.getTable(User.class)
        .select().field("AUTHID").eq(mClient.getCurrentUser().getUserId()).execute();
    Futures.addCallback(future, new FutureCallback<MobileServiceList<User>>() {
      @Override
      public void onSuccess(MobileServiceList<User> result) {
        if (result.isEmpty()) {
          registerUser();
        } else {
          cacheUserToken(mClient.getCurrentUser(), result.get(0).nickname);
          loadUsers();
        }
      }

      @Override
      public void onFailure(Throwable t) {
        Toast toast = Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_SHORT);
        toast.show();
      }
    });
  }

  private void cacheUserToken(MobileServiceUser user, String nickname) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(USERIDPREF, user.getUserId());
    editor.putString(TOKENPREF, user.getAuthenticationToken());
    editor.putString(NICKNAMEPREF, nickname);
    authId = user.getUserId();
    editor.commit();
  }

  private boolean loadUserTokenCache(MobileServiceClient client) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    String userId = prefs.getString(USERIDPREF, null);
    if (userId == null)
      return false;
    String token = prefs.getString(TOKENPREF, null);
    if (token == null)
      return false;

    MobileServiceUser user = new MobileServiceUser(userId);
    user.setAuthenticationToken(token);
    client.setCurrentUser(user);
    authId = userId;

    return true;
  }

  public void registerUser() {
    final User item = new User();
    item.authID = mClient.getCurrentUser().getUserId();
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Input a nickname");

    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);
    builder.setCancelable(false);
    builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        item.nickname = input.getText().toString();
        saveNewUser(item);
      }
    });

    builder.show();
  }

  public void saveNewUser(final User user) {
    ListenableFuture<User> future = mClient.getTable(User.class).insert(user);
    Futures.addCallback(future, new FutureCallback<User>() {
      @Override
      public void onSuccess(User result) {
        authId = user.authID;
        loadUsers();
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

  public class User {
    public String Id;
    public String authID;
    public String nickname;

    @Override
    public String toString() {
      return nickname;
    }
  }

  private void authenticate() {
    if (!loadUserTokenCache(mClient)) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Sign In");
      builder.setCancelable(false);
      builder.setPositiveButton("Microsoft", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          mClient.login("MicrosoftAccount", "chat", MICROSOFT_LOGIN_REQUEST_CODE);

        }
      });
      builder.setNegativeButton("Google", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          mClient.login("Google", "chat", MICROSOFT_LOGIN_REQUEST_CODE);
        }
      });
      builder.show();
    } {
      loadUsers();
    }
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
          mClient.getCurrentUser();
          lookUpUser();
        } else {
          // login failed, check the error message
          text = result.getErrorMessage();
        }
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
      }
    }
  }


  private void openConversation(User user) {
    Intent intent = new Intent(this, ConversationActivity.class);
    intent.putExtra(MESSAGE_AUTH, user.authID);
    intent.putExtra(MESSAGE_NICK, user.nickname);
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
