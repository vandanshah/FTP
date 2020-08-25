/*Author: Vandan Shah, MeetKumar Patel, Rushiraj Jadeja, Viraj Paragaonkar */

/* This is the server file. It has all major functions performed by the server. */

package ftp.server;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import static java.nio.file.StandardCopyOption.*;

/* Main class which runs the file */

public class FTPServer {
	static int PortNo;

	public static void main(String args[]) throws Exception {

		/* set the port */

		if (args.length == 1) {
			PortNo = Integer.parseInt(args[0]);
		} else {
			PortNo = 5217;
		}

		/* Initiate the Server Socket */
		ServerSocket controlSoc = new ServerSocket(PortNo);
		ServerSocket dataSoc = new ServerSocket(PortNo - 1);

		System.out.println("FTP Server Started on Port Number " + PortNo);

		/* Waiting for Client */

		while (true) {
			System.out.println("Waiting for Connection ...");
			Communication c = new Communication(controlSoc.accept(), dataSoc);
		}
	}
}

/*
 * Class which has all the server side methods. Maintains transaction with
 * client.
 */

class Communication extends Thread {
	static Socket ClientSoc, dataSoc;
	static ServerSocket DataSoc;
	DataInputStream dinput;
	DataOutputStream doutput;

	/* Connects with client */

	Communication(Socket soc, ServerSocket datasoc) {
		try {
			ClientSoc = soc;
			DataSoc = datasoc;
			dinput = new DataInputStream(ClientSoc.getInputStream());
			doutput = new DataOutputStream(ClientSoc.getOutputStream());
			System.out.println("FTP Client Connected ...");
			String use = dinput.readUTF();
			if (use.compareTo("vandanshah@outlook.com") == 0) {
				String pass = dinput.readUTF();
				if (pass.compareTo("3011") == 0) {
					doutput.writeUTF("Success");
					System.out.println("User logged in successfully");

				} else {
					doutput.writeUTF("Failure");
				}

			} else {
				doutput.writeUTF("Failure");
			}
			start();

		} catch (Exception ex) {
		}
	}

	/* Sends File in response of GET command from Client. */

	void SendFile() throws Exception {
		try {
			DataInputStream datain;
			DataOutputStream dataout;
			dataSoc = DataSoc.accept();
			datain = new DataInputStream(dataSoc.getInputStream());
			dataout = new DataOutputStream(dataSoc.getOutputStream());

			String filename = dinput.readUTF();
			File f = new File(filename);
			double filelength = f.length();
			double updatelength = filelength / 1000;
			if (!f.exists()) {
				doutput.writeUTF("File Not Found");
				return;
			} else {
				doutput.writeUTF("READY");
				FileInputStream fin = new FileInputStream(f);
				int ch, count = 0;
				do {
					if (count > updatelength) {
						updatelength += updatelength;
					}
					count++;
					ch = fin.read();
					dataout.writeUTF(String.valueOf(ch));
				} while (ch != -1);
				fin.close();
				doutput.writeUTF("File Received Successfully");
				doutput.writeUTF("Bytes Transfered: " + filelength);
				doutput.writeUTF("File Name: " + f.getName());

			}
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}
	}

	/* Receives file in response of SEND command from Client */

	void ReceiveFile() throws Exception {
		try {
			DataInputStream datain;
			DataOutputStream dataout;
			dataSoc = DataSoc.accept();
			datain = new DataInputStream(dataSoc.getInputStream());
			dataout = new DataOutputStream(dataSoc.getOutputStream());

			String filename = dinput.readUTF();
			if (filename.compareTo("File not found") == 0) {
				return;
			}
			File f = new File(filename);
			String option;

			if (f.exists()) {
				doutput.writeUTF("File Already Exists");
				option = dinput.readUTF();
			} else {
				doutput.writeUTF("SendFile");
				option = "Y";
			}

			if (option.compareTo("Y") == 0) {

				File tmpfile = new File("E:\\" + f.getName());

				FileOutputStream fout = new FileOutputStream(tmpfile);
				int ch;
				String temp;
				System.out.println("File Receiving Started.");
				do {
					temp = datain.readUTF();
					ch = Integer.parseInt(temp);
					if (ch != -1) {
						fout.write(ch);
					}
				} while (ch != -1);
				fout.close();
				doutput.writeUTF("File Sent Successfully");
				doutput.writeUTF("Bytes Transfered: " + tmpfile.length());
				doutput.writeUTF("File Name: " + f.getName());
			} else {
				return;
			}
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}
	}

	/* Returns file from root directory in response of LS command from client */

	void getFiles() throws Exception {
		try {
			DataInputStream datain;
			DataOutputStream dataout;
			dataSoc = DataSoc.accept();
			datain = new DataInputStream(dataSoc.getInputStream());
			dataout = new DataOutputStream(dataSoc.getOutputStream());
			File folder = new File("E:\\Arduino Tutorial\\");
			File[] listofFiles = folder.listFiles();

			int count = 0;
			for (int i = 0; i < listofFiles.length; i++) {
				if (listofFiles[i].isFile()) {
					count++;
				}
			}
			doutput.writeInt(count);
			System.out.println(count);
			for (File file : listofFiles) {
				if (file.isFile()) {
					doutput.writeUTF(file.getName());
				}

			}
			doutput.writeUTF("Command executed successfully.");
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}
	}

	@SuppressWarnings("deprecation")

	/* Senses and Identifies the client's command */

	public void run() {

		while (true) {
			try {

				System.out.println("Waiting for Command ...");
				String Command = dinput.readUTF();
				if (Command.compareTo("GET") == 0) {
					System.out.println("\tGET Command Received ...");
					SendFile();
					continue;
				} else if (Command.compareTo("PUT") == 0) {
					System.out.println("\tSEND Command Receiced ...");
					ReceiveFile();
					continue;
				} else if (Command.compareTo("DISCONNECT") == 0) {
					System.out.println("\tDisconnect Command Received ...");
					doutput.flush();
					ClientSoc.close();
					stop();
				} else if (Command.compareTo("LS") == 0) {
					System.out.println("\tgetFiles Command Received ...");
					getFiles();
					continue;
				}

			} catch (Exception ex) {
			}
		}
	}
}
