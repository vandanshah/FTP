/*Author: Vandan Shah, MeetKumar Patel, Rushiraj Jadeja, Viraj Paragaonkar */

/* Client side file which has all client side's functions */
package ftp.client;

import java.net.*;
import java.io.*;
import java.util.*;

/* Main class having main method */

public class FTPClient {

	public static Socket controlSoc;
	public static DataInputStream din = null;
	public static DataOutputStream dout = null;
	public static DataInputStream datadin = null;
	public static DataOutputStream dataout = null;
	public static Socket dataSoc;
	public static int PortNo, dataPort;
	public static String Host;

	/* Establish connection with Server */

	public static String Connect() throws Exception {
		try {
			String username = "";
			String pass = "";
			String msg = "Failure";
			System.out.println("Enter Host Address: ");
			Scanner sc = new Scanner(System.in);
			String host = sc.nextLine();
			System.out.println("Enter Port No: ");
			Scanner sc1 = new Scanner(System.in);
			String port = sc1.nextLine();
			System.out.println("Enter User Name: ");
			Scanner sc2 = new Scanner(System.in);
			String userName = sc2.nextLine();
			System.out.println("Enter Password: ");
			Scanner sc3 = new Scanner(System.in);
			String password = sc3.nextLine();
			Host = host;
			PortNo = Integer.parseInt(port);
			username = userName;
			pass = password;
			controlSoc = new Socket(Host, PortNo);
			dataPort = PortNo - 1;
			din = new DataInputStream(controlSoc.getInputStream());
			dout = new DataOutputStream(controlSoc.getOutputStream());
			dout.writeUTF(username);
			dout.writeUTF(pass);
			return din.readUTF();
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
			return din.readUTF();
		}
	}

	/* Disconnects the connection from Server */

	public static void disconnect() throws IOException {

		dout.writeUTF("DISCONNECT");

		System.out.println("Connection closed");
	}

	/* PUT command of FTP */

	public static void put(File file) throws Exception {
		dout.writeUTF("PUT");
		dataSoc = new Socket(Host, dataPort);
		datadin = new DataInputStream(dataSoc.getInputStream());
		dataout = new DataOutputStream(dataSoc.getOutputStream());
		String filename = file.getName();
		dout.writeUTF(filename);

		String msgFromServer = din.readUTF();
		if (msgFromServer.compareTo("File Already Exists") == 0) {
			String Option;
			System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
		}

		System.out.println("Sending File ...");
		System.out.println();
		FileInputStream fin = new FileInputStream(file);
		double filelength = file.length();
		double updatelength = filelength / 1000;
		int ch, count = 0;
		do {
			if (count > updatelength) {
				updatelength += updatelength;

			}
			count++;
			ch = fin.read();
			System.out.print("* ");
			dataout.writeUTF(String.valueOf(ch));
		} while (ch != -1);
		fin.close();
		System.out.println();
		System.out.println(din.readUTF());
		System.out.println(din.readUTF());
		System.out.println(din.readUTF());
		disconnect();
	}

	/* GET Command of FTP */

	public static void get(File file) throws Exception {
		try {
			dout.writeUTF("GET");
			dataSoc = new Socket(Host, dataPort);
			datadin = new DataInputStream(dataSoc.getInputStream());
			dataout = new DataOutputStream(dataSoc.getOutputStream());
			dout.writeUTF(file.getPath());
			String msgFromServer = din.readUTF();

			if (msgFromServer.compareTo("File Not Found") == 0) {
				System.out.println("File not found on Server ...");
				return;
			} else if (msgFromServer.compareTo("READY") == 0) {
				System.out.println("Receiving File ...");
				System.out.println();
				File f = new File("F:\\" + file.getName());
				FileOutputStream fout = new FileOutputStream(f);
				int ch;
				String temp;
				do {
					temp = datadin.readUTF();
					ch = Integer.parseInt(temp);
					if (ch != -1) {
						fout.write(ch);
						System.out.print("* ");
					}
				} while (ch != -1);
				fout.close();
				System.out.println();
				System.out.println(din.readUTF());
				System.out.println(din.readUTF());
				System.out.println(din.readUTF());
				disconnect();
			}
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}

	}

	/* LS command of FTP */

	public static void ls() throws IOException {
		try {
			dout.writeUTF("LS");
			dataSoc = new Socket(Host, dataPort);
			datadin = new DataInputStream(dataSoc.getInputStream());
			dataout = new DataOutputStream(dataSoc.getOutputStream());
			int length = din.readInt();
			System.out.println("Count is : " + length);
			for (int i = 0; i < length; i++) {
				System.out.println(din.readUTF());
			}
			disconnect();
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}

	}

	public static void main(String[] args) {
		String msg = "";
		try {
			msg = Connect();
			System.out.println("Enter command:");
			Scanner sc = new Scanner(System.in);
			String command = sc.nextLine();
			if (command.compareTo("PUT") == 0) {
				System.out.println("Enter File Path:");
				Scanner fileScanner = new Scanner(System.in);
				File f = new File(fileScanner.nextLine());
				put(f);
			} else if (command.compareTo("GET") == 0) {
				System.out.println("Enter File Path: ");
				Scanner fileScanner = new Scanner(System.in);
				File f = new File(fileScanner.nextLine());
				get(f);
			} else if (command.compareTo("LS") == 0) {
				ls();
			}

		} catch (Exception exc) {

		}
	}
}
