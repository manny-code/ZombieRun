package servidor;


import java.util.*;
import java.io.IOException;
import comunicacion.EscenarioBean;
import clasesPrincipales.*;

public class Partida {

	// DETALLES ESTATICOS DE LA PARTIDA
	private int id;
	private String nombre;
	private int cantJugadoresMax;
	private int cantJugadoresMin;
	
	
	// DETALLES DINAMICOS DE LA PARTIDA
	private boolean estado; // 0 REGISTRANDO - 1 EN CURSO
	private int cantJugadoresEnCurso;
	private Figura[][] escenario;
	private ArrayList<Jugador>jugadores;
	private int tamX;
	private int tamY;
	private int empiezaZombie = 1;
	///////////////MAPAS////////////////////////
	
	private static final char[][] murosMapa1 ={
		{'X','X','X','X','X','X','X','X','X','X'},
		{'X',' ',' ',' ',' ',' ',' ','X',' ','X'},
		{'X',' ','X',' ','X',' ',' ',' ',' ','X'},
		{'X',' ','X',' ','X',' ','X','X','X','X'},
		{'X',' ','X','X','X',' ',' ',' ',' ','X'},
		{'X',' ',' ',' ',' ',' ',' ',' ',' ','X'},
		{'X',' ','X',' ','X','X','X',' ','X','X'},
		{'X',' ','X',' ','X','X','X',' ','X','X'},
		{'X',' ','X',' ',' ',' ',' ',' ',' ','X'},
		{'X','X','X','X','X','X','X','X','X','X'}
	};
	
	////////////////////////////////////////////
	
	
	
	
	public Partida(int i, String nom, int cantMax, int cantMin, int idMapa ){
		id = i;
		setNombre(nom);
		cantJugadoresMax = cantMax;
		setCantJugadoresMin(cantMin);
		cantJugadoresEnCurso = 0;
		jugadores = new ArrayList<Jugador>();
		
		
		if( idMapa == 1 ){
			tamX = 10;
			tamY = 10;
			escenario = new Figura[10][10];
			for (int j = 0; j < 10; j++) {
				for (int j2 = 0; j2 < 10; j2++) {
					if( murosMapa1[j][j2] == 'X'){
						escenario[j][j2] = new Bloque();
					}
				}
			}
		}
		
	}
	
	public void agregarJugador( Jugador jugador ) throws InterruptedException, IOException{
		if( cantJugadoresEnCurso < cantJugadoresMax ){
			jugadores.add(jugador);
			cantJugadoresEnCurso++;
			jugador.getOut().writeObject((Integer)cantJugadoresEnCurso); // LO USO PARA IDENTIFICAR AL JUGADOR
			System.out.println( "CANTIDAD DE JUGADORES EN CURSO : " + cantJugadoresEnCurso );
			
			if( cantJugadoresEnCurso == cantJugadoresMax ){
				estado = true; // LA PARTIDA ESTA EN CURSO
				generarPosiciones();
				enviarComienzo();
				esperarJugadores();
				enviarMapa();
				comenzarPartida();
			}
			
		}else{
			System.out.println("CAPACIDAD MAXIMA PARTIDA : " + this.id);
		}
	}
	
	
	public int getId() {
		return id;
	}

	public int getCantJugadoresMax() {
		return cantJugadoresMax;
	}

	public int getCantJugadoresEnCurso() {
		return cantJugadoresEnCurso;
	}

	public int getTamX() {
		return tamX;
	}

	public int getTamY() {
		return tamY;
	}

	private void esperarJugadores() {
		Thread hilo = new Thread( new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean jugadorNoListo = true;
				System.out.println("VERIFICANDO QUE ESTE LISTO");
				while( jugadorNoListo == true ){
					jugadorNoListo = false;
					//System.out.println("verificando...");
					for (Jugador jugador : jugadores) {
						if( jugador.isEstoyListo() == false )
							jugadorNoListo = true;
					}
				}
				try {
					this.finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		hilo.start();
	}

	private void enviarComienzo() {
		// TODO Auto-generated method stub
		System.out.println("PREPARANDO A LOS JUGADORES...");
		for (Jugador jugador : jugadores) {
			try {
				jugador.getOut().writeObject(new EscenarioBean(escenario, tamX, tamY));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private void comenzarPartida() {
		Thread hiloPartida = new Thread( new Runnable(){
			public void run(){
				while( true ){
					try {
						while(true){
							Thread.sleep(5000);
							pedirDirecciones();// MANDAMOS UN MENSAJE A LOS CLIENTES PARA RECIBIR LAS DIRECCIONES
							esperarDirecciones();
							moverJugadores();
							detectarInfecciones();				
							enviarMapa();
						}
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		hiloPartida.start();
	}

	protected void detectarInfecciones() {
		// TODO Auto-generated method stub
		for (Jugador jugador : jugadores) {
			int x = jugador.getX();
			int y = jugador.getY();
			if( ((Personaje)escenario[x][y]).esZombie() == false ){
				if( escenario[x+1][y] instanceof Personaje && 
						((Personaje)escenario[x+1][y]).esZombie() == true){
					((Personaje)escenario[x][y]).convertir();
				}
				if( escenario[x-1][y] instanceof Personaje &&
						((Personaje)escenario[x-1][y]).esZombie() == true){
					((Personaje)escenario[x][y]).convertir();
				}
				if( escenario[x][y-1] instanceof Personaje &&
						((Personaje)escenario[x][y-1]).esZombie() == true){
					((Personaje)escenario[x][y]).convertir();
				}
				if( escenario[x][y+1] instanceof Personaje &&
						((Personaje)escenario[x][y+1]).esZombie() == true){
					((Personaje)escenario[x][y]).convertir();
				}
			}
		}
		
	}

	protected void moverJugadores() {
		for (Jugador jugador : jugadores) {
			int x = jugador.getX();
			int y = jugador.getY();
			System.out.println( "JugadorX :" +x);
			System.out.println( "JugadorY :" +y);
			System.out.println("************** entra ****************");
			if( jugador.getDireccion() == 1 ){// ARRIBA
				if( escenario[x][y-1] == null ){
					System.out.println("SE MOVIO HACIA ARRIBA");
					escenario[x][y-1] = escenario[x][y];
					escenario[x][y] = null;
					jugador.setY(jugador.getY()-1);
				}
			}
			if( jugador.getDireccion() == 2 ){// ABAJO
				if( escenario[x][y+1] == null ){
					escenario[x][y+1] = escenario[x][y];
					escenario[x][y] = null;
					jugador.setY(jugador.getY()+1);
				}
			}
			if( jugador.getDireccion() == 3 ){//IZQUIERDA
				if( escenario[x-1][y] == null ){
					escenario[x-1][y] = escenario[x][y];
					escenario[x][y] = null;
					jugador.setX(jugador.getX()-1);
				}
			}
			if( jugador.getDireccion() == 4 ){//DERECHA
				if( escenario[x+1][y] == null ){
					escenario[x+1][y] = escenario[x][y];
					escenario[x][y] = null;
					jugador.setX(jugador.getX()+1);
				}
			}
		}
	}

	protected void esperarDirecciones() {
		Thread hilo = new Thread( new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean jugadorNoListo = true;
				System.out.println("Verificando que haya enviado direccion");
				while( jugadorNoListo == true ){
					jugadorNoListo = false;
					for (Jugador jugador : jugadores) {
						if( jugador.isEnvieDireccion() == false )
							jugadorNoListo = true;
					}
				}
				
				for (Jugador jugador : jugadores) {
					jugador.setEnvieDireccion(false);
				}
				
				try {
					this.finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		hilo.start();
		
	}

	protected void pedirDirecciones() throws IOException {
		for (Jugador jugador : jugadores) {
			System.out.println("Pido direccion para el jugador ");
			jugador.getOut().reset();
			jugador.getOut().writeObject("DIRECCION");
			jugador.getOut().flush();
		}
		
	}

	public void enviarMapa(){ // POR CADA JUGADOR DE EL ARRAYLIST ENVIO EL MAPA
		System.out.println("ENVIANDO MAPA A LOS JUGADORES...");
		for (Jugador jugador : jugadores) {
			try {
				jugador.getOut().reset();
				jugador.getOut().writeObject(new EscenarioBean(escenario, tamX, tamY));
				jugador.getOut().flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void generarPosiciones(){
		Random rand = new Random();
		int cont = 1;
		for (Jugador jugador : jugadores) {
			boolean seColoco = false;
			while( seColoco == false ){
				int x = rand.nextInt()%murosMapa1.length;
				System.out.println(x);
				int y = rand.nextInt()%murosMapa1.length;
				System.out.println(y);
				if( x >= 0 && y >= 0 && escenario[x][y] == null ){
					if( cont == empiezaZombie )
						escenario[x][y] = new Personaje(jugador.getNick(), x, y, true);
					else
						escenario[x][y] = new Personaje(jugador.getNick(), x, y, false);
					jugador.setX(x);
					jugador.setY(y);
					seColoco = true;
				}
			}
			cont++;
		}
	}
	
	
	

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getCantJugadoresMin() {
		return cantJugadoresMin;
	}

	public void setCantJugadoresMin(int cantJugadoresMin) {
		this.cantJugadoresMin = cantJugadoresMin;
	}

	public boolean isEstado() {
		return estado;
	}

	public void setEstado(boolean estado) {
		this.estado = estado;
	}

	public Figura[][] getEscenario() {
		return escenario;
	}

	public void setEscenario(Figura[][] escenario) {
		this.escenario = escenario;
	}

	public ArrayList<Jugador> getJugadores() {
		return jugadores;
	}
	
}
	

	
	

