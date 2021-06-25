import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] arg) {
        System.out.print("\033[H\033[2J");
        try {
            int port = 10000;
            ExecutorService pool = Executors.newFixedThreadPool(50);

            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {

                final Socket connection = serverSocket.accept();
                pool.execute(() -> {
                    try {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String client_mess;
                        while ((client_mess = br.readLine()) != null) {
                            System.out.println(connection);
                            JSONParser jsonParser = new JSONParser();
                            JSONObject rec = (JSONObject) jsonParser.parse(client_mess);
                            String function = (String) rec.get("function");
                            JSONObject content = (JSONObject) rec.get("content");
                            switch (function) {
                                case "login": {
                                    String username = (String) content.get("username");
                                    String password = (String) content.get("password");
                                    System.out.println(username + " is trying to login.");
                                    Connection con;
                                    Class.forName("org.postgresql.Driver");
                                    con = DriverManager.getConnection("jdbc:postgresql://47.119.141.11:5432/info", "user", "user123456");
                                    String sql = "SELECT login('" + username + "','" + password + "');";
                                    Statement st = con.createStatement();
                                    ResultSet rs = st.executeQuery(sql);
                                    int res = 3;
                                    while (rs.next()) res = rs.getInt(1);
                                    switch (res) {
                                        case 0: {
                                            System.out.println("registration succeed.");
                                        }
                                            break;
                                        case 1: {
                                            System.out.println("login succeed");
                                        }
                                            break;
                                        case 2: {
                                            System.out.println("login failed.");
                                        }
                                            break;
                                    }
                                    con.close();
                                    JSONObject ret = new JSONObject();
                                    ret.put("function", "login-rep");
                                    ret.put("timestamp", java.util.Calendar.getInstance().getTime().toString());
                                    ret.put("content", res);
                                    OutputStream outputStream = connection.getOutputStream();
                                    Writer writer = new OutputStreamWriter(outputStream);
                                    writer.write(ret.toString());
                                    writer.flush();
                                    connection.shutdownOutput();
                                }
                                    break;
                            }
                        }
                    } catch (IOException | ParseException | ClassNotFoundException | SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            connection.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
