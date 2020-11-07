package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClientHandler {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    DataInputStream in;
    DataOutputStream out;
    Server server;
    Socket socket;

    private String nickname;
    private String login;

    private ExecutorService service = Executors.newCachedThreadPool();

    public ClientHandler(Server server, Socket socket) {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            logger.severe("Client connected " + socket.getRemoteSocketAddress());


            service.execute(() -> {
                try {
                    socket.setSoTimeout(120000);
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/reg ")){
                            String[] token = str.split("\\s");
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = server.getAuthService().registration(token[1], token[2],token[3]);
                            if (b){
                                sendMsg("/regok");
                            } else {
                                sendMsg("/regno");
                            }
                        }

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s");
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1]; // присваиваем для login введенный пользователем логин
                                if(!server.isLoginAuthenticated(login)){ // если такого пользователя еще нет в чате, то даем авторизоваться
                                    nickname = newNick;

                                    sendMsg("/authok " + newNick);
                                    server.subscribe(this);

                                    break;
                                } else{
                                    sendMsg("Под текущим логином уже вошли в чат\n");
                                }

                            } else {
                                logger.info("Пользователь login " + login + " ввел неверный логин или пароль");
                                sendMsg("Неверный логин / пароль\n");
                            }
                        }
                    }
                    //цикл работы

                    socket.setSoTimeout(0);

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }


                            // смена ника
                            if (str.startsWith("/chnick ")){
                                String[] token = str.split("\\s");
                                if (token.length != 2) {
                                    continue;
                                }
                                String newNick = server.getAuthService().changeNickname(login, token[1]);
                                if (newNick != null){
                                    server.serverMessage(this, "сменил ник на", newNick);
                                    nickname = newNick;
                                    server.broadcastClientList();

                                } else{
                                    server.serverMessage(this, "Такой никнейм уже занят");
                                }
                            }

                            if (str.startsWith("/w")) {
                                String[] token = str.split("\\s", 3); // разделяет на 3 элемента: /w, получатель и текст сообщения
                                if (token.length < 3){
                                    continue;
                                }
                                server.privateMsg(this, token[1],token[2]);
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                }
                catch (SocketTimeoutException e){
                    sendMsg("/end");
                    logger.severe("Пользователь отключен по таймауту " + socket.getRemoteSocketAddress());
                }
                catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    logger.severe("Client disconnected " + socket.getRemoteSocketAddress());
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        logger.warning("Ошибка закрытия сокета");
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

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
