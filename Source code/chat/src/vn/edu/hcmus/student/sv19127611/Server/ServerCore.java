package vn.edu.hcmus.student.sv19127611.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
/**
 * vn.edu.hcmus.student.sv19127611.Server
 * Created by fminhtu
 * Date 12/30/2021 - 10:13 PM
 * Description: ...
 */
public class ServerCore {
	private Object sharedObjects;
	private ServerSocket s;
	private Socket socket;
	private int port = 8080;

	public static ArrayList<Handler> clients = new ArrayList<Handler>();
	private final String accountFIle = "resources\\accounts.txt";

	static final String ONLINE_USERS = "Online users";
	static final String LOGIN = "Log in";
	static final String REGISTER = "Sign up";
	static final String SPLIT = "#==";

	private void loadAccounts() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(accountFIle), StandardCharsets.UTF_8));
			String rawAccount = br.readLine();

			while (rawAccount != null && !(rawAccount.isEmpty())) {
				String[] account = rawAccount.split(SPLIT);
				clients.add(new Handler(account[0], account[1], false, sharedObjects));
				rawAccount = br.readLine();
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveAccounts() {
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(accountFIle, StandardCharsets.UTF_8);

			for (Handler client : clients) {
				printWriter.print(client.getUsername() + SPLIT + client.getPassword() + "\n");
			}

			printWriter.println("");
			printWriter.close();
		} catch (Exception ex ) {
			System.out.println(ex.getMessage());
		}

	}
	
	public ServerCore() throws IOException {
		try {
			sharedObjects = new Object();
			this.loadAccounts();
			s = new ServerSocket(port);
			
			while (true) {
				socket = s.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				String request = dis.readUTF();

				switch (request) {
					case REGISTER: {
						String username = dis.readUTF();
						String password = dis.readUTF();

						if (!isExisted(username)) {
							Handler handler = new Handler(socket, username, password, true, sharedObjects);
							clients.add(handler);
							this.saveAccounts();

							dos.writeUTF("Sign up successful");
							dos.flush();

							Thread t = new Thread(handler);
							t.start();

							updateOnlineUsers();
						} else {
							dos.writeUTF("This username is being used");
							dos.flush();
						}
						break;
					}

					case LOGIN: {
						String username = dis.readUTF();
						String password = dis.readUTF();

						if (isExisted(username)) {
							for (Handler client : clients) {
								if (client.getUsername().equals(username)) {
									if (password.equals(client.getPassword())) {
										client.setSocket(socket);
										client.setIsLoggedIn(true);

										dos.writeUTF("Log in successful");
										dos.flush();

										Thread t = new Thread(client);
										t.start();

										updateOnlineUsers();
									} else {
										dos.writeUTF("Password is not correct");
										dos.flush();
									}
									break;
								}
							}
						} else {
							dos.writeUTF("This username is not exist");
							dos.flush();
						}
						break;
					}
				}
				
			}

		} catch (Exception ex) {
			System.err.println(ex);

		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	public boolean isExisted(String name) {
		for (Handler client:clients) {
			if (client.getUsername().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static void updateOnlineUsers() {
		String message = " ";

		for (Handler client:clients) {
			if (client.getIsLoggedIn()) {
				message +=  SPLIT;
				message += client.getUsername();
			}
		}

		for (Handler client:clients) {
			if (client.getIsLoggedIn()) {
				try {
					client.getDos().writeUTF(ONLINE_USERS);
					client.getDos().writeUTF(message);
					client.getDos().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

class Handler implements Runnable{
	private final Object lock;

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;

	private String username, password;
	private boolean isLoggedIn;

	static final String EXIT = "Log out";
	static final String MESSAGE = "Text";
	static final String FILE = "File";
	static final String SAFE_TO_LEAVE = "Safe to leave";

	public Handler(Socket socket, String username, String password, boolean isLoggedIn, Object lock) throws IOException {
		this.socket = socket;
		this.username = username;
		this.password = password;
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.isLoggedIn = isLoggedIn;
		this.lock = lock;
	}
	
	public Handler(String username, String password, boolean isLoggedIn, Object lock) {
		this.username = username;
		this.password = password;
		this.isLoggedIn = isLoggedIn;
		this.lock = lock;
	}
	
	public void setIsLoggedIn(boolean IsLoggedIn) {
		this.isLoggedIn = IsLoggedIn;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeSocket() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean getIsLoggedIn() {
		return this.isLoggedIn;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public DataOutputStream getDos() {
		return this.dos;
	}
	
	@Override
	public void run() {
		label:
		while (true) {
			try {
				if (dis.available() > 0) {
					String message = null;
					message = dis.readUTF();

					switch (message) {
						case EXIT:
							dos.writeUTF(SAFE_TO_LEAVE);
							dos.flush();
							socket.close();
							this.isLoggedIn = false;

							ServerCore.updateOnlineUsers();
							break label;

						case MESSAGE: {
							String receiver = dis.readUTF();
							String content = dis.readUTF();

							for (Handler client : ServerCore.clients) {
								if (client.getUsername().equals(receiver)) {
									synchronized (lock) {
										client.getDos().writeUTF(MESSAGE);
										client.getDos().writeUTF(this.username);
										client.getDos().writeUTF(content);
										client.getDos().flush();
										break;
									}
								}
							}
							break;
						}

						case FILE: {
							String receiver = dis.readUTF();
							String filename = dis.readUTF();
							int size = Integer.parseInt(dis.readUTF());
							int bufferSize = 2048;
							byte[] buffer = new byte[bufferSize];

							for (Handler client : ServerCore.clients) {
								if (client.getUsername().equals(receiver)) {

									synchronized (lock) {
										client.getDos().writeUTF(FILE);
										client.getDos().writeUTF(this.username);
										client.getDos().writeUTF(filename);
										client.getDos().writeUTF(String.valueOf(size));

										while (size > 0) {
											dis.read(buffer, 0, Math.min(size, bufferSize));
											client.getDos().write(buffer, 0, Math.min(size, bufferSize));
											size -= bufferSize;
										}
										client.getDos().flush();
										break;
									}
								}
							}
							break;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
