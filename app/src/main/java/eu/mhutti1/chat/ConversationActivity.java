package eu.mhutti1.chat;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.query.ExecutableQuery;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.util.ArrayList;
import java.util.List;



public class ConversationActivity extends AppCompatActivity {

  public static final String SENDER_ID = "22693517818";

  public static List<Message> conversation = new ArrayList<>();
  public static ChatAdapter conversationAdapter;
  String remoteNickname;
  public static String remoteId;
  public static String localId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    conversation.clear();
    setContentView(R.layout.activity_conversation);
    remoteNickname = getIntent().getStringExtra(MainActivity.MESSAGE_NICK);
    remoteId = getIntent().getStringExtra(MainActivity.MESSAGE_AUTH);
    localId = Utils.myId(getApplicationContext());
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(remoteNickname); // Set title to conversation partners nickname
    NotificationsManager.handleNotifications(this, SENDER_ID, PushHandler.class);

    ListView conversationListView = (ListView) findViewById(R.id.conversation);
    conversationAdapter = new ChatAdapter(this, android.R.layout.simple_list_item_1, conversation);
    conversationListView.setAdapter(conversationAdapter);
    TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.message_box);
    textInputLayout.getEditText().setOnEditorActionListener(new EditText.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEND) {
          Message message = new Message(localId, remoteId, textView.getText().toString());
          conversation.add(message);
          conversationAdapter.notifyDataSetChanged();
          postMessage(message);
          textView.setText("");
          return true;
        }
        return false;
      }
    });
    loadMessages();

  }

  public void postMessage(final Message message) {
    ListenableFuture<Message> future = MainActivity.mClient.getTable(Message.class).insert(message);
    Futures.addCallback(future, new FutureCallback<Message>() {
      @Override
      public void onSuccess(Message result) {
      }

      @Override
      public void onFailure(Throwable t) {
        Toast toast = Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_SHORT);
        toast.show();
      }
    });
  }

  public void loadMessages() {
    ExecutableQuery from =
        new ExecutableQuery<>().field("SENDER").eq(remoteId).and().field("RECIPIENT").eq(localId);
    ExecutableQuery to =
        new ExecutableQuery<>().field("SENDER").eq(localId).and().field("RECIPIENT").eq(remoteId);
    ListenableFuture<MobileServiceList<Message>> future = MainActivity.mClient
        .getTable(Message.class).execute(from.or(to));
    Futures.addCallback(future, new FutureCallback<MobileServiceList<Message>>() {
      @Override
      public void onSuccess(MobileServiceList<Message> result) {
        for (Message message : result) {
          conversation.add(message);
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

}
