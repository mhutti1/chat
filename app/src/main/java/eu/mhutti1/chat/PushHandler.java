package eu.mhutti1.chat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

/**
 * Created by Isaac on 16/06/2017.
 */

public class PushHandler extends NotificationsHandler {
  public static final int NOTIFICATION_ID = 1;

  @Override
  public void onRegistered(Context context, final String gcmRegistrationId) {
    super.onRegistered(context, gcmRegistrationId);

    new AsyncTask<Void, Void, Void>() {

      protected Void doInBackground(Void... params) {
        try {
          MainActivity.mClient.getPush().register(gcmRegistrationId);
          return null;
        } catch (Exception e) {
          // handle error
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void onReceive(Context context, Bundle bundle) {
    String sender = bundle.getString("sender");
    String recipient = bundle.getString("recipient");
    String text = bundle.getString("text");

    Message msg = new Message(sender, recipient, text);

    if (ConversationActivity.localId.equals(recipient) || ConversationActivity.remoteId.equals(sender)) {
      ConversationActivity.conversation.add(msg);
      ConversationActivity.conversationAdapter.notifyDataSetChanged();
    }
  }
}
