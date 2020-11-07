package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.*;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SQLiteAuthService();
        ServerSocket server = null;
        Socket socket = null;
        final int PORT = 8189;
        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            server = new ServerSocket(PORT);
            logger.info("Server started");

            while (true) {
                socket = server.accept();
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            logger.warning("### Сервер не начал работу ###");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warning("### Не закрылся socket ###");
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                logger.warning("### Не закрылся server ###");
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[ %s ]: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
        logger.fine(message);
    }

    // МЕТОД ПРИВАТНОГО СООБЩЕНИЯ
    public void privateMsg(ClientHandler sender, String reciever, String msg) {
        String message = String.format("[ %s ] private [ %s ]: %s", sender.getNickname(), reciever, msg);
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(reciever)){
                c.sendMsg(message); // отправляем сообщение получателю, если в списке он есть
                if (!sender.getNickname().equals(reciever)){ // если получатель - не сам отправитель, то и себе отправляем сообщение
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg(String.format("Server: client %s is not found", reciever)); // если такой отправитель не найден, отправляем сообщение отправителю
        logger.fine(message);
    }

    public void serverMessage(ClientHandler sender, String msg, String newNick){
        sender.sendMsg(msg);
        String message = String.format("[ %s ]: %s [ %s ]", sender.getNickname(), msg, newNick);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
        logger.fine("Server message: " + message);
    }

    public void serverMessage(ClientHandler sender, String msg) {
        sender.sendMsg(msg);
        logger.fine("Server message: " + msg);
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated(String login){
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)){
                logger.info(login + " не прошел аутентификацию");
                return true;
            }
        }
        logger.info(login + " прошел аутентификацию");
        return false;
    }

    // ОТПРАВКА СПИСКА АВТОРИЗОВАННЫХ ПОЛЬЗОВАТЕЛЕЙ В ПРАВОЕ ОКНО В СТОЛБИК ПОСЛЕ ВХОДА И ВЫХОДА ИЗ ЧАТА ЛЮБОГО ПОЛЬЗОВАТЕЛЯ
    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist "); // создаем изменяемую строку, которая будет начинаться с /clientlist_
        for (ClientHandler c : clients) {
            sb.append(c.getNickname()).append(" "); // добавляем в sb никнеймы авторизованных пользователей: /clienlist qwe asd zxc
        }
        sb.setLength(sb.length() - 1); // убираем лишний пробел в конце
        String message = sb.toString(); // переводим в формат String для отправки всем пользователям списка
        for (ClientHandler c : clients) {
            c.sendMsg(message); // отправляем сообщение
        }
        logger.info("Создан clientlist");
    }
}
