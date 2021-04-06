package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private ServerChat server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(ServerChat server, Socket socket) {

        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() ->{

                try {
                    while (true){
                        String str = in.readUTF();
                        if(str.startsWith("/auth ")){
                            String[] tokens = str.split(" ");
                            String nick = AuthSetvice.getNicknameByLoginAndPassword(
                                    tokens[1], tokens[2]);
                            if (nick != null){
                                sendMSG("/auth-OK");
                                setNickname(nick);
                                server.subscribe(ClientHandler.this);
                                break;
                            } else {
                                sendMSG("Wrong Login/password");
                            }
                        }
                    }

                    while (true){
                        String str = in.readUTF();
                        if(str.equals("/end")){
                            out.writeUTF("/serverClosed");
                            System.out.println("Client disconnected\n" + socket.getInetAddress());
                            break;
                        }

                        System.out.println("Client " + socket.getInetAddress()
                                        + "\n" + str);
//                        out.writeUTF("     MSG ECHO : " + str);
                        server.broadcastMSG(str);

                    }
                } catch (IOException e){
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                server.unsubscribe(this);

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void setNickname(String nick) {
        this.nickname = nick;
    }

    public void sendMSG (String str){
     try {
         out.writeUTF(str);
     } catch (IOException e) {
         e.printStackTrace();
     }
 }
}
