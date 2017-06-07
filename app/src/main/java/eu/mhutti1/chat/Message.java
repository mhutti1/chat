package eu.mhutti1.chat;

import java.util.Date;

/**
 * Created by Isaac on 06/06/2017.
 */

public class Message {
  public final String sender;
  public final String recipient;
  public final String time;
  public final String text;

  public Message(String sender, String recipient, String text) {
    this.sender = sender;
    this.recipient = recipient;
    this.text = text;
    this.time = String.valueOf(new Date().getTime());
  }
}
