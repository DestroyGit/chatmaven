package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements Initializable {
    @FXML
    private ListView<String> clientList; //добавили <String>, а то при стандарте не добавляется функционал. Бех него просто объекты хранятся, а нам надо String
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private HBox authPanel;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox msgPanel;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private Stage stage;
    private Stage regStage;
    private RegController regController;

    private boolean authenticated;
    private String nickname;
    private String login;

    private InputStreamReader history = null;
    private FileOutputStream saveHistory = null;

    private ExecutorService service = Executors.newCachedThreadPool();


    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
            setTitle("Балабол");
        } else {
            setTitle(String.format("[ %s ] - Балабол", nickname));
        }
        textArea.clear();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();

            stage.setOnCloseRequest(event -> { // отключаться от сервера, если авторизован, при нажатии на крестик
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
        createRegWindow();
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            service.execute(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/regok")) {
                            regController.addMessageTextArea("Регистрация прошла успешно");
                        }
                        if (str.startsWith("/regno")) {
                            regController.addMessageTextArea("Регистрация не прошла");
                        }

                        if (str.startsWith("/authok")) {
                            nickname = str.split("\\s")[1];
                            setAuthenticated(true);
                            break;
                        }

                        textArea.appendText(str + "\n");
                    }

                    // обращение к файлу, хранящему историю переписок, и печать на экран этой истории
                    saveHistory = new FileOutputStream(linkHistory(login), true);
                    try {
                        textArea.appendText(return100rollsOfHistory(login));
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }

                    //цикл работы

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/clientlist")) { // обновляем лист с авторизованными пользователями
                                String[] token = str.split("\\s");
                                Platform.runLater(() -> { // так как графическую часть затрагиваем, используем это
                                    clientList.getItems().clear(); // очищаем поле с никами, кто авторизовался
                                    for (int i = 1; i < token.length; i++) { // /clientlist qwe asd zxc - это 0 1 2 3 элементы, нам надо с 1 элемента
                                        clientList.getItems().add(token[i]); // добавляем в поле с никами авторизованные ники

                                    }
                                });
                            }
                        } else {
                            textArea.appendText(str + "\n");
                            saveHistory.write(str.getBytes());
                            saveHistory.write("\n".getBytes());
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Отключен по таймауту");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        saveHistory.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            service.shutdown();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        if (textField.getText().trim().length() == 0) {
            return;
        }
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        this.login = loginField.getText().trim();

        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());
        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    public void clickClientList(MouseEvent mouseEvent) { // куча методов у mouseEvent. (точка). Посмотреть документацию!!! Попробовать самому, поэкспериментировать
        textField.setText(String.format("/w %s ", clientList.getSelectionModel().getSelectedItem())); // находим никнейм в списке авторизованных пользователей
    }

    private void createRegWindow() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml")); // надо создать новый FXML loader
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Регистрация");
            regStage.setScene(new Scene(root, 400, 300));
            regStage.initModality(Modality.APPLICATION_MODAL); // нельзя перейти в то окно, из которого это окно было вызвано
            regController = fxmlLoader.getController(); // создание ссылки на RegController
            regController.setController(this); // установить в RegController ссылку на Controller
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void regStageShow(ActionEvent actionEvent) {
        regStage.show(); // показать окно при нажатии на кнопку "reg"
    }

    public void tryRegistration(String login, String password, String nickname) {
        this.login = login;
        String message = String.format("/reg %s %s %s", login, password, nickname);
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String linkHistory(String login){
        return "history/history_[" + login + "].txt";
    }


    // метод возврата последних 100 строк истории сообщений
    private String  return100rollsOfHistory(String login) throws IOException {
        if (!Files.exists(Paths.get(linkHistory(login)))){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<String> historyLines = Files.readAllLines(Paths.get(linkHistory(login)));
        int start = 0;
        if (historyLines.size() > 100){
            start = historyLines.size() - 100;
        }
        for (int i = start; i < historyLines.size(); i++) {
            sb.append(historyLines.get(i)).append(System.lineSeparator());
        }
        return sb.toString();
    }


}
