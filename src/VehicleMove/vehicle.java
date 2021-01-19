package VehicleMove;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.awt.event.MouseEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class vehicle {

	private JFrame frmVehicle;
	private JLabel lblVehicle;
	private JLabel lblVehicleNew;
	private JComboBox cmbVehicle;
	private JTextField txtAddress;
	private JLabel lblStatus;
	
	private Socket connection;

	int x;
	int y;

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					vehicle window = new vehicle();
					window.frmVehicle.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public vehicle() {
		initialize();
	}

	
	private void initialize() {
		frmVehicle = new JFrame();
		frmVehicle.getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				x = e.getX();
				y = e.getY();
				moveVehicle(x,y);
			}
		});
		frmVehicle.setTitle("Vehicle");
		frmVehicle.setBounds(100, 100, 499, 356);
		frmVehicle.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmVehicle.getContentPane().setLayout(null);
		
		txtAddress = new JTextField();
		txtAddress.setBounds(27, 24, 130, 26);
		frmVehicle.getContentPane().add(txtAddress);
		txtAddress.setColumns(10);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				connectToHost();
				frmVehicle.getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
					public void mouseMoved(MouseEvent e) {
						x = e.getX();
						y = e.getY();
						moveVehicle(x,y);
						SendMove();
					}
				});
				
			}
		});
		btnConnect.setBounds(177, 22, 117, 29);
		frmVehicle.getContentPane().add(btnConnect);
		
		JLabel lblOr = new JLabel("Or");
		lblOr.setHorizontalAlignment(SwingConstants.CENTER);
		lblOr.setBounds(304, 28, 29, 16);
		frmVehicle.getContentPane().add(lblOr);
		
		JButton btnHost = new JButton("Host");
		btnHost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				hostANetwork();
				frmVehicle.getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
					public void mouseMoved(MouseEvent e) {
						x = e.getX();
						y = e.getY();
						moveVehicle(x,y);
						if(connection != null) {
							SendMove();
						}
					}
				});
				
			}
		});
		btnHost.setBounds(346, 22, 117, 29);
		frmVehicle.getContentPane().add(btnHost);
		
		lblStatus = new JLabel("Status: disconnected");
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setBounds(27, 92, 200, 16);
		frmVehicle.getContentPane().add(lblStatus);
		
		lblVehicle = new JLabel("");
		lblVehicle.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/car.png"));
		lblVehicle.setBounds(281, 180, 171, 106);
		frmVehicle.getContentPane().add(lblVehicle);
		
		cmbVehicle = new JComboBox();
		cmbVehicle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				changeVehicle();
				
			}
		});
		cmbVehicle.setModel(new DefaultComboBoxModel(new String[] {"Car", "Motorbike"}));
		cmbVehicle.setSelectedIndex(0);
		cmbVehicle.setBounds(252, 80, 145, 40);
		frmVehicle.getContentPane().add(cmbVehicle);
		
		lblVehicleNew = new JLabel("");
		lblVehicleNew.setBounds(x, y, lblVehicle.getWidth(), lblVehicle.getHeight());
		frmVehicle.getContentPane().add(lblVehicleNew);
		lblVehicleNew.setVisible(false);
	}
	

	private void hostANetwork() {
		Thread thread = new NetworkHostingThread();
		thread.start();
	}
	
	private void connectToHost() {	
		Thread thread = new ConnectionThread();
		thread.start();
	}
	
	
	private class NetworkHostingThread extends Thread {
		@Override
		public void run() {
			super.run();
			
			try {
				ServerSocket serverSocket = new ServerSocket(9999);
				connection = serverSocket.accept();
				NewVehicle();
				lblStatus.setText("Status: Connected");
				Thread thread = new MoveAsPartnerThread();
				thread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ConnectionThread extends Thread {
		@Override
		public void run() {
			super.run();
			
			String hostAddress = txtAddress.getText();
			try {
				connection = new Socket(hostAddress, 9999);
				NewVehicle();
				lblStatus.setText("Status: Connected");
				Thread thread = new MoveAsPartnerThread();
				thread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private class MoveAsPartnerThread extends Thread {
		@Override
		public void run() {
			super.run();
			
			try {
				InputStream inputStream = connection.getInputStream();
				Scanner scanner = new Scanner(inputStream);
				while(true) {
					String message = scanner.nextLine();
					String[] parts = message.split("##");
					String  type = parts[0];
					String dataString = parts[1];
					List<String> data = null;
					if(!dataString.isEmpty()) {
						data = new ArrayList<>();
						String[] dataParts = dataString.split("#");
						for(String itemPart : dataParts) {
							String readyItem = itemPart.replace("#", "");
							data.add(readyItem);
						}
					}
					int X = Integer.parseInt(data.get(0));
					int Y = Integer.parseInt(data.get(1));
					if(type.equals("Car")) {
						lblVehicleNew.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/car.png"));
					} else {
						lblVehicleNew.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/motorbike.png"));
					}
					lblVehicleNew.setBounds(X, Y, lblVehicleNew.getWidth(), lblVehicleNew.getHeight());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	private void SendMove() {
		Thread thread = new SendMoveThread();
		thread.start();
	}
	
	private class SendMoveThread extends Thread {
		@Override
		public void run() {
			super.run();
			
			try {
				List<Integer> params=new ArrayList<Integer>();  
				params.add(x);
				params.add(y);
				String vehicletype = cmbVehicle.getSelectedItem().toString();
				OutputStream outputStream = connection.getOutputStream();
				PrintWriter printWriter = new PrintWriter(outputStream);
				String paramsString = "";
				String movement = "";
					if(params != null) {
						for(int i=0; i<params.size();i++) {
							if(i == (params.size()-1)) {
								paramsString += params.get(i);
							}else {
								paramsString += params.get(i) + "#";
							}
						}
					}
					movement = vehicletype + "##" + paramsString + "\r\n";
					printWriter.write(movement);
					printWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void NewVehicle() {
		lblVehicleNew.setVisible(true);
	}
	
	private void changeVehicle() {
		String vehicle = cmbVehicle.getSelectedItem().toString();
		if(vehicle.equals("Car")) {
			lblVehicle.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/car.png"));
		} else {
			lblVehicle.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/motorbike.png"));
		}
	}
	
	private void moveVehicle(int x, int y) {
		lblVehicle.setBounds(x, y, lblVehicle.getWidth(), lblVehicle.getHeight());
	}
	
}