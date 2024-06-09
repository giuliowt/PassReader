package pack;

import com.google.common.hash.Hashing;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.text.html.ImageView;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Iterator;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private TextField userInput;  //Scene 1

    @FXML
    public PasswordField passwordInput;  //Scene 1

    @FXML
    public PasswordField NewPasswordInput;  //Scene 2

    @FXML
    public TextField visiblePasswordInput;  //Scene 2

    @FXML
    public CheckBox PasswordBox;  //Scene 1

    @FXML
    public Label errorLabel;  //Scene 1

    @FXML
    public Label errorLabelRegister;  //Scene 2

    @FXML
    public TextField newUsernameInput;  //Scene 2

    @FXML
    public TextField newVisiblePasswordInput;  //Scene 2

    @FXML
    public TextField addKey;  //Scene 3

    @FXML
    public TextField addPassword;  //Scene 3

    @FXML
    public Button addBtn;  //Scene 3

    @FXML
    public Button remBtn;  //Scene 3

    @FXML
    public CheckBox NewAccountBox;  //Scene 2

    @FXML
    public Button yesBtn;  //Alert

    @FXML
    public Button closeBtn;  //Close scene

    @FXML
    public TableView<Product> passwordView = new TableView();  //Scene 3

    @FXML
    public TableColumn KeyColumn = new TableColumn<Product, String>();  //Scene 3

    @FXML
    public TableColumn PasswordColumn = new TableColumn<Product, String>();  //Scene 3

    private String Username;
    private String Password;
    private JSONParser jsonParser = new JSONParser();
    private String actual;

    public void Login() {
        decryptJSON();
        try {
            Stage actualWindow = (Stage) passwordInput.getScene().getWindow();
            JSONObject jsonObject = (JSONObject)jsonParser.parse(new FileReader("Credentials.json"));
            Username = jsonObject.get("Username").toString();
            Password = jsonObject.get("Password").toString();

            if (toSha256(userInput.getText()).equals(Username)
                && toSha256(passwordInput.getText()).equals(Password)) {
                errorLabel.setVisible(false);
                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Passwords");
                window.setResizable(false);
                Parent layout = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
                window.setScene(new Scene(layout));
                window.show();
                window.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::OnCloseAction);
            } else {
                errorLabel.setVisible(true);
            }
        } catch (Exception e) {
            errorLabel.setVisible(true);
        }
    }

    public void OnCloseAction(WindowEvent event) {
        encryptJSON();
    }

    public void showPassword(){  //Scene 1
        visiblePasswordInput.setText(passwordInput.getText());
    }

    public void newAccountShowPassword(){  //Scene 2
        newVisiblePasswordInput.setText(NewPasswordInput.getText());
    }

    public void hidePassword(){  //Scene 1
        passwordInput.setText(visiblePasswordInput.getText());
    }

    public void newAccountHidePassword(){  //Scene 2
        NewPasswordInput.setText(newVisiblePasswordInput.getText());
    }

    public void ActionPasswordBox(){  //Scene 1
        if (PasswordBox.isSelected()){
            visiblePasswordInput.setVisible(true);
            passwordInput.setVisible(false);
        } else {
            visiblePasswordInput.setVisible(false);
            passwordInput.setVisible(true);
        }
    }

    public void NewActionPasswordBox() {  //Scene 2
        if (NewAccountBox.isSelected()){
            NewPasswordInput.setVisible(false);
            newVisiblePasswordInput.setVisible(true);
        } else {
            NewPasswordInput.setVisible(true);
            newVisiblePasswordInput.setVisible(false);
        }
    }

    public void newAccount() throws IOException {  //Scene 2
        Stage actualWindow = (Stage) yesBtn.getScene().getWindow();
        actualWindow.close();
        openWindow("Register.fxml", "Register");
    }

    public void Alert() throws IOException {
        openWindow("Alert.fxml", "Warning");
    }


    public void registerNewAccount() throws IOException, ParseException {  //Scene 2
        JSONObject newJson = new JSONObject();
        JSONObject newJsonArray = new JSONObject();
        newJson.put("Username", "");
        newJson.put("Passwords", newJsonArray);
        newJson.put("Password", "");
        FileWriter fileWriter = new FileWriter("Credentials.json");
        fileWriter.write(newJson.toJSONString());
        fileWriter.close();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("Credentials.json"));
        JSONObject jsonArray = (JSONObject) jsonObject.get("Passwords");
        if (NewPasswordInput.getLength() >= 8) {
            jsonArray.keySet().clear();
            FileWriter file = new FileWriter("Credentials.json");
            jsonObject.put("Username", toSha256(newUsernameInput.getText()));
            jsonObject.put("Password", toSha256(NewPasswordInput.getText()));
            file.write(jsonObject.toString());
            file.flush();
            Stage actualWindow = (Stage) newUsernameInput.getScene().getWindow();
            actualWindow.close();
        } else {
            errorLabelRegister.setVisible(true);
        }
    }

    public void dontRegisterNewAccount() {
        Stage actualWindow = (Stage) yesBtn.getScene().getWindow();
        actualWindow.close();
    }


    public String toSha256(String hashed) {
        return Hashing.sha256().hashString(hashed, StandardCharsets.UTF_8).toString();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        KeyColumn.setCellValueFactory(new PropertyValueFactory("key"));
        PasswordColumn.setCellValueFactory(new PropertyValueFactory("password"));
        KeyColumn.setReorderable(false);
        PasswordColumn.setReorderable(false);
    }

    boolean canAdd = true;
    public void takePasswordFromJSON() throws IOException, ParseException {
        if (canAdd) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("Credentials.json"));
            JSONObject jsonArray = (JSONObject) jsonObject.get("Passwords");
            Iterator iterator = jsonArray.keySet().iterator();
            while (iterator.hasNext()) {
                actual = (String) iterator.next();
                passwordView.getItems().add(new Product(actual, jsonArray.get(actual).toString()));
            }
            try {
                encryptJSON();
            } catch (Exception e) {}
            canAdd = false;
        }
    }


    String oldKey = "ŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅŅ";
    public void addPassword() {
        try {
            decryptJSON();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("Credentials.json"));
            JSONObject jsonArray = (JSONObject) jsonObject.get("Passwords");
            Iterator iterator = jsonArray.keySet().iterator();
            boolean register = true;
            while (iterator.hasNext()) {
                if (addKey.getText().equals(iterator.next())) {
                    register = false;
                    openWindow("Error.fxml", "Error");
                    return;
                }
            }
            if (register && !addKey.getText().isBlank() && !addPassword.getText().isBlank()
                && addKey.getText().length() <= 35 && addPassword.getText().length() <= 35) {
                passwordView.getItems().add(new Product(addKey.getText(), addPassword.getText()));
                jsonArray.put(addKey.getText(), addPassword.getText());
                FileWriter file = new FileWriter("Credentials.json");
                file.write(jsonObject.toString());
                file.flush();
            } else {
                openWindow("Error.fxml", "Error");
            }
            oldKey = addKey.getText();
            try {
                encryptJSON();
            } catch (Exception e){}
        } catch (Exception e) {}
    }

    public void removePassword() throws IOException, ParseException {
        decryptJSON();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("Credentials.json"));
        JSONObject jsonArray = (JSONObject) jsonObject.get("Passwords");
        Product p = passwordView.getSelectionModel().getSelectedItem();
        passwordView.getItems().removeAll(passwordView.getSelectionModel().getSelectedItems());
        try {
            jsonArray.remove(p.getKey());
            FileWriter file = new FileWriter("Credentials.json");
            file.write(jsonObject.toString());
            file.flush();
        } catch (Exception e) {}
        encryptJSON();
    }

    public void closeAlert(){
        Stage actualWindow = (Stage) closeBtn.getScene().getWindow();
        actualWindow.close();
    }

    public void openWindow(String fxml, String title) throws IOException {
        Stage alert = new Stage();
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        alert.setResizable(false);
        Parent layout = FXMLLoader.load(getClass().getResource(fxml));
        alert.setScene(new Scene(layout));
        alert.show();
    }

    public String encrypt(String message, String key) throws Exception {
        byte[] byteFile = message.getBytes();
        byte[] byteKey = key.getBytes();

        Key secretKey = new SecretKeySpec(byteKey, "AES");

        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipher = c.doFinal(byteFile);

        String encryptedMessage = Base64.getEncoder().encodeToString(cipher);
        return encryptedMessage;
    }

    public String encryptJSON() {
        try {
            String passwordToKey = "";
            try {
                passwordToKey = toSha256(passwordInput.getText());
            } catch (Exception e){}
            FileReader fileReader = new FileReader(new File("Credentials.json"));
            int line = 0;
            char value;
            String message = "";
            while (line != -1) {
                line = fileReader.read();
                if (line != -1) {
                    value = (char) line;
                    message += value;
                }
            }
            String encryptedMessage = "";
            try {
                encryptedMessage = encrypt(message, passwordToKey.substring(0,32));
                FileWriter file = new FileWriter("Credentials.json");
                file.write(encryptedMessage);
                file.flush();
            } catch (Exception exception) {}
            return encryptedMessage;
        } catch (Exception e) {}
        return "JSON encrypted";
    }


    public String decrypt(String encryptedMessage, String key) throws Exception {
        byte[] byteKey = key.getBytes();

        Key secretKey = new SecretKeySpec(byteKey, "AES");

        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedMessage = Base64.getDecoder().decode(encryptedMessage);
        byte[] byteMessage = c.doFinal(decryptedMessage);

        String message = new String (byteMessage);
        return message;
    }

    public String decryptJSON() {
        try {
            String passwordToKey = toSha256(passwordInput.getText());
            FileReader fileReader = new FileReader(new File("Credentials.json"));
            int line = 0;
            char value;
            String message = "";
            while (line != -1) {
                line = fileReader.read();
                if (line != -1) {
                    value = (char) line;
                    message += value;
                }
            }
            String decryptedMessage = decrypt(message, passwordToKey.substring(0, 32));
            FileWriter file = new FileWriter("Credentials.json");
            file.write(decryptedMessage);
            file.flush();
            return decryptedMessage;
        } catch (Exception e) {}
        return "JSON decrypted";
    }

}
