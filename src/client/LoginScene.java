package client;

import static pulpcore.image.Colors.WHITE;
import static pulpcore.image.Colors.rgb;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import pulpcore.CoreSystem;
import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.scene.Scene2D;
import pulpcore.sprite.Button;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Sprite;
import pulpcore.sprite.StretchableSprite;
import pulpcore.sprite.TextField;

public class LoginScene extends Scene2D implements Serializable, LoginHandler {

   private static final long serialVersionUID = 5402874044677852559L;

   private ConnectionHandler connection;

   private Label lbl;

   private TextField tf;

   private Button button;

   private String userName;

   private Properties props;

   public void load() {
      CoreFont font = CoreFont.load("imgs/FS.font.png");
      CoreFont fontW = font.tint(WHITE);

      connection = new ConnectionHandler();
      connection.setLoginHandler(this);

      FilledSprite fill = new FilledSprite(Colors.rgb(0x3F94F3));
      fill.fillColor.animateTo(Colors.DARKGRAY, 12000);
      add(fill);

      add(new StretchableSprite("imgs/border.9.png", 185, 152, 350, 150));

      lbl = new Label(fontW, "", 210, 260);
      add(lbl);

      add(new Label(fontW, "Username: ", 200, 170));

      tf = new TextField(font, fontW, "guest", 290, 170, 150, fontW.getHeight());
      tf.setFocus(true);
      tf.setMaxNumChars(20);
      add(createTextFieldBackground(tf));
      add(tf);

      button = Button.createLabeledButton("Login", 286, 205);
      button.setKeyBinding(Input.KEY_ENTER);
      add(button);

      // connect to server
      props = new Properties();
      props.put("host", CoreSystem.getBaseURL().getHost());
      props.put("port", CoreSystem.getAppProperty("port"));
   }

   private Sprite createTextFieldBackground(TextField field) {
      field.selectionColor.set(rgb(0x1d5ef2));
      ImageSprite background = new ImageSprite("imgs/textfield.png", field.x
            .get() - 5, field.y.get() + 8);
      background.setAnchor(Sprite.WEST);
      return background;
   }

   @Override
   public void update(int elapsedTime) {
      if (button.isClicked()) {
         if (tf.getText().trim().length() > 0) {
            userName = tf.getText();
            connection.setUserName(tf.getText());
            connection.setPassword(tf.getText().toCharArray());
            lbl.setText("Connecting...");

            try {
               connection.login(props);
            }
            catch (IOException e) {
               log(e.getMessage());
            }
         }
         else {
            lbl.setText("Please enter user name");
         }
      }
   }

   @Override
   public void loggedIn() {
      invokeLater(new Runnable() {
         @Override
         public void run() {
            Stage.setScene(new LobbyScene(userName, connection));
         }
      });
   }

   @Override
   public void loginFailed(String reason) {
      log(reason);
   }

   /* - BaseHandle - */
   @Override
   public void incomingChat(String str) {
   }

   @Override
   public void log(final String str) {
      invokeLater(new Runnable() {
         @Override
         public void run() {
            lbl.setText(str);
         }
      });
   }
}