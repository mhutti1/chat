package mhutti1.eu.chat;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Isaac on 06/06/2017.
 */

public class ChatAdapter extends ArrayAdapter<Message> {
  public ChatAdapter(@NonNull Context context, @LayoutRes int resource) {
    super(context, resource);
  }

  public ChatAdapter(@NonNull Context context, @LayoutRes int resource, List<Message> messages) {
    super(context, resource, messages);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;

    if (view == null) {
      view = LayoutInflater.from(getContext()).inflate(R.layout.message, null);
    }

    Message message = getItem(position);

    if (message != null) {
      TextView textView = (TextView) view.findViewById(R.id.message_textview);
      textView.setText(message.text);
      if (message.sender.equals(Utils.myId(getContext()))) {
        textView.setGravity(Gravity.START);
      } else {
        textView.setGravity(Gravity.END);
      }
    }
    return view;
  }
}
