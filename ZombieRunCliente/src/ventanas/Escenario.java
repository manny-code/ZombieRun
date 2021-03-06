package ventanas;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import java.awt.Color;

import clasesPrincipales.*;
import comunicacion.*;

import javax.swing.JButton;
import javax.swing.ImageIcon;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class Escenario extends JFrame {

	private static final long serialVersionUID = 4013032887277483935L;

	private JPanel contentPane;

	//PANEL DONDE SE DIBUJA EL ESCENARIO
	private JPanel escenario; 
	
	// SE ALMACENA HACIA DONDE SE QUIERE MOVER EL JUGADOR Y SE INICIALIZA CADA VEZ QUE LLEGA EL MAPA
	private int direccion; // 0 NO SE MOVIO - 1 ARRIBA - 2 ABAJO - 3 IZQUIERDA - 4 DERECHA
	
	// OBJETO EL CUAL NOS PERMITE MANTENER LA COMUNICACION CON EL SERVIDOR
	private Cliente clientSocket;
	
	// TIMER
	private JLabel lblTimer;
	private Timer timer;
	
	// LOBBY
	private Lobby lobby;
	
	//IMAGENES
	private  final ImageIcon humano = new ImageIcon( getClass().getResource("/imagenes/humano.jpg"));
	private  final ImageIcon zombie = new ImageIcon( getClass().getResource("/imagenes/zombie.jpg"));
	private  final ImageIcon muro = new ImageIcon( getClass().getResource("/imagenes/muro.jpg"));

	private  final ImageIcon humanoP = new ImageIcon( getClass().getResource("/imagenes/humanoP.jpg"));
	private  final ImageIcon zombieP = new ImageIcon( getClass().getResource("/imagenes/zombieP.jpg"));
	
	
	
	private int idPartida;
	
	public Escenario( Cliente client, int idP, Lobby l) {
		lobby = l;
		idPartida = idP;
		direccion = 0;
		clientSocket = client;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnIzquierda = new JButton("");
		btnIzquierda.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				direccion = 3;
				System.out.println("DIRECCION : " + direccion);
			}
		});
		btnIzquierda.setIcon(new ImageIcon(Escenario.class.getResource("/imagenes/izquierda.jpg")));
		btnIzquierda.setSelectedIcon(null);
		btnIzquierda.setBounds(26, 498, 50, 23);
		contentPane.add(btnIzquierda);
		
		JButton btnAbajo = new JButton("");
		btnAbajo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				direccion = 2;
				System.out.println("DIRECCION : " + direccion);
			}
		});
		btnAbajo.setIcon(new ImageIcon(Escenario.class.getResource("/imagenes/abajo.jpg")));
		btnAbajo.setBounds(86, 521, 50, 30);
		contentPane.add(btnAbajo);
		
		JButton btnDerecha = new JButton("");
		btnDerecha.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				direccion = 4;
				System.out.println("DIRECCION : " + direccion);
			}
		});
		btnDerecha.setIcon(new ImageIcon(Escenario.class.getResource("/imagenes/derecha.jpg")));
		btnDerecha.setBounds(146, 498, 50, 23);
		contentPane.add(btnDerecha);
		
		JButton btnArriba = new JButton("");
		btnArriba.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				direccion = 1;
				System.out.println("DIRECCION : " + direccion);
			}
		});
		btnArriba.setIcon(new ImageIcon(Escenario.class.getResource("/imagenes/arriba.jpg")));
		btnArriba.setBounds(86, 472, 50, 30);
		contentPane.add(btnArriba);
		
		escenario = new JPanel();
		escenario.setBackground(Color.BLACK);
		escenario.setLayout(null);
			
		
		lblTimer = new JLabel("4");
		lblTimer.setBounds(335, 236, 46, 14);
		lblTimer.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblTimer.setForeground(Color.WHITE);
		contentPane.add(lblTimer);
	
		
		contentPane.add(escenario);
		dibujarEscenario();
		HiloDeJuego();
		
	}
	
	
	// ESTE METODO SE ENCARGA DE DIBUJAR EL MAPA
	public void dibujarEscenario(){


		Figura[][] mapa = ((EscenarioBean)clientSocket.leerMensaje()).getMapa();
		int tamX = ((EscenarioBean)clientSocket.leerMensaje()).getTamX();
		int tamY = ((EscenarioBean)clientSocket.leerMensaje()).getTamY();
		int xp = ((EscenarioBean)clientSocket.leerMensaje()).getX();
		int yp = ((EscenarioBean)clientSocket.leerMensaje()).getY();
		
		escenario.removeAll();
		escenario.setBounds(10, 10, tamX*25, tamY*25);
		
		if( ((Personaje)mapa[xp][yp]).esZombie() == false ){
			for (int i = 0; i < mapa.length; i++) {
				for (int j = 0; j < mapa.length; j++) {
					if( xp-2 <= i && xp+2 >= i && yp-2 <= j && yp+2 >=j){
						if( mapa[i][j] instanceof Bloque ){
							JLabel muroAux = new JLabel(muro);
							muroAux.setBounds(i*25, j*25, 25, 25);
							escenario.add(muroAux);
						}else if( mapa[i][j] instanceof Personaje ){
							if( ((Personaje)mapa[i][j]).esZombie() ){
								if( i == xp && j == yp ){
									JLabel muroAux = new JLabel(zombieP);
									muroAux.setBounds(i*25, j*25, 25, 25);
									escenario.add(muroAux);
								}else{
									JLabel muroAux = new JLabel(zombie);
									muroAux.setBounds(i*25, j*25, 25, 25);
									escenario.add(muroAux);
								}
							}else{
								if( i == xp && j == yp ){
									JLabel muroAux = new JLabel(humanoP);
									muroAux.setBounds(i*25, j*25, 25, 25);
									escenario.add(muroAux);
								}else{
									JLabel muroAux = new JLabel(humano);
									muroAux.setBounds(i*25, j*25, 25, 25);
									escenario.add(muroAux);
								}
							}
						}
					}
				}
			}
		}else{
			for (int i = 0; i < mapa.length; i++) {
				for (int j = 0; j < mapa.length; j++) {
					
					if( mapa[i][j] instanceof Bloque ){
						JLabel muroAux = new JLabel(muro);
						muroAux.setBounds(i*25, j*25, 25, 25);
						escenario.add(muroAux);
					}else if( mapa[i][j] instanceof Personaje ){
						if( ((Personaje)mapa[i][j]).esZombie() ){
							if( i == xp && j == yp ){
								JLabel muroAux = new JLabel(zombieP);
								muroAux.setBounds(i*25, j*25, 25, 25);
								escenario.add(muroAux);
							}else{
								JLabel muroAux = new JLabel(zombie);
								muroAux.setBounds(i*25, j*25, 25, 25);
								escenario.add(muroAux);
							}
						}else{
							if( i == xp && j == yp ){
								JLabel muroAux = new JLabel(humanoP);
								muroAux.setBounds(i*25, j*25, 25, 25);
								escenario.add(muroAux);
							}else{
								JLabel muroAux = new JLabel(humano);
								muroAux.setBounds(i*25, j*25, 25, 25);
								escenario.add(muroAux);
							}
						}
					}
				}
			}
		}
			
			
		escenario.revalidate();
		escenario.repaint();
	}

	

	public void HiloDeJuego(){
		Thread hiloDeJuego = new Thread( new Runnable(){
			public void run(){
				
				while( true ){
				
					Object peticion = clientSocket.escuchar();
		
					if( peticion instanceof EscenarioBean ){
						System.out.println("Se recibio el escenario");
				
						dibujarEscenario();
						//direccion = 0;
					}else if( peticion.equals("DIRECCION")){
						System.out.println("NOS PIDIERON DIRECCION");
						try {
							
							clientSocket.getOut().writeObject(new DireccionBean(direccion,clientSocket.getJugador(), idPartida ));
							clientSocket.getOut().flush();
							System.out.println("ENVIAMOS DIRECCION : " + direccion);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// ARREGLAR.. NO COORDINA BIEN
						timer = new Timer(1000, new ActionListener() {			
							int elapsedSeconds = 3;
							@Override
							public void actionPerformed(ActionEvent e) {
								elapsedSeconds--;
						        lblTimer.setText(Integer.toString(elapsedSeconds));
						        if(elapsedSeconds == 0){
						           elapsedSeconds = 3;
						           timer.restart();
						        }
							}
						});
						timer.start();
					}else if( peticion.equals("TERMINO")){
						/*System.out.println("SE TERMINO LA PARTIDA");
						lobby.setVisible(true);
						try {
							
							dispose();
							System.out.println(" CIERRO ESCENARIO");
							this.finalize();	
							break;
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
					}

				}// FIN WHILE TRUE
				
			}
		});
		hiloDeJuego.start();
	}

}