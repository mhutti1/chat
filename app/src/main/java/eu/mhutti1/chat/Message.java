package eu.mhutti1.chat;

import java.util.Date;

/**
 * Created by Isaac on 06/06/2017.
 */

public class Message {
  public String id;
  public String sender;
  public String recipient;
  public String text;

  public Message(String sender, String recipient, String text) {
    this.sender = sender;
    this.recipient = recipient;
    this.text = text;
  }
}
