package Cliente;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;

import Servidor.BaseDeDatos;

public class TriquiTraque implements Runnable{
	//Declaramos las variables necesarias para la conexion y comunicacion
	private String nombre;
	private Socket cliente;
	private PrintWriter out;
	private BufferedReader in;
	private String mensaje;
	private String turno;
	private boolean verTurno;
	private String jugada;
	private String estado;
	public static final String GANASTE="Ganaste";
	public static final String PERDISTE="Perdiste";
	public static final String EMPATE="Empate";
	public static final String JUEGA="Juega";
	//El puerto debe ser el mismo en el que escucha el servidor
	private int puerto;
	//Si estamos en nuestra misma maquina usamos localhost si no la ip de la maquina servidor
	private String host = "localhost";
	//Variables para cargar las imagenes de la X y O
	private String imgX;
	private String imgO;
	//Conexion con la BD
	private BaseDeDatos bd;

	/**
	 * Constructor de la clase
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws SQLException 
	 */
	public TriquiTraque(int puertoP, String hostP) throws UnknownHostException, IOException{
		imgX="./data/X.jpg";
		imgO="./data/O.jpg";
		puerto=puertoP;
		host=hostP;
		
		cliente=new Socket(hostP, puertoP);
		in=new BufferedReader(new InputStreamReader(cliente.getInputStream()));
		out=new PrintWriter(cliente.getOutputStream(), true);
		
		bd=new BaseDeDatos();
	}
	/**
	 * Metodo implementado para los hilos
	 */
	public void run() {
		try {
			mensaje=in.readLine();
			String[] cadena = mensaje.split(":");
			turno=cadena[0].split(" ")[1];
			estado=turno;
			verTurno=Boolean.valueOf(cadena[1]);
			String clien1="";
			String clien2="";
			if(mensaje.startsWith("Cliente1")){
				String[] cad=mensaje.split(" ");
				nombre=cad[1];
				clien1=registrarCliente(nombre);
			}else if(mensaje.startsWith("Cliente2")){
				String[] cad=mensaje.split(" ");
				nombre=cad[1];
				clien2=registrarCliente(nombre);
			}
			//Recibe los movimientos de los jugadores
			while(true){
				//Leer mensaje
				mensaje = in.readLine();
				/*
                El mensaje esta compuesto por una cadena separada por ; cada separacion representa un dato
                    mensaje[0] : representa X o O que es el tipo de ficha en el tablero.
                    mensaje[1] : representa fila del tablero
                    mensaje[2] : representa columna del tablero
                    mensaje[3] : representa estado del juego [Perdiste, Ganaste, Empate]
				 */
				String[] mensajes = mensaje.split(":");
				int tipo = Integer.parseInt(mensajes[0]);
				int fila = Integer.parseInt(mensajes[1]);
				int columna = Integer.parseInt(mensajes[2]);
				
				//Marca la casilla con una jugada
				if(tipo==1) jugada(fila, columna, imgX);
				else jugada(fila, columna, imgO);
				
				//Actualiza el estado del juego
				if(turno.equals(mensajes[3])) {
					estado=GANASTE;
					if(verTurno==true && !clien1.equals("")) registrarVictoria(clien1);
					else if(!clien2.equals("")) registrarVictoria(clien2);
				}
				else if(EMPATE.equals(mensajes[3])) {
					estado=EMPATE;
					registrarEmpate(clien1, clien2);
				}
				else if(!JUEGA.equals(mensajes[3])) {
					estado=PERDISTE;
					if(verTurno==true && !clien1.equals("")) registrarPerdida(clien1);
					else if(!clien2.equals("")) registrarPerdida(clien2);
				}
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void registrarEmpate(String clien1, String clien2) throws SQLException {
		bd.registrarEmpate(clien1, clien2);
	}
	
	private void registrarPerdida(String clien) throws SQLException {
		bd.registrarDerrota(clien);
	}
	
	private void registrarVictoria(String clien) throws SQLException {
		bd.registrarVictoria(clien);
	}
	/**
	 * Devuelve los datos de los jugadores
	 * @throws SQLException 
	 */
	public String[] darRegistrosJugadores() throws SQLException{
		return bd.consultarRegistrosJugadores();
	}
	/**
	 * Registra un nuevo jugador
	 * @param nombre2 del jugador
	 * @throws SQLException
	 */
	private String registrarCliente(String nombre2) throws SQLException {
		return bd.registarJugador(nombre2);
	}
	/**
	 * Envía el turno al servidor
	 */
	public void enviarTurno(int x,int y)throws Exception{
		/*
	        Comprobamos que sea nuestro turno para jugar, si no es devolmemos un mensaje
	        Si es el turno entonces mandamos un mensaje al servidor con los datos de la jugada que hicimos
		 */
		if(verTurno){
			String datos="";
			datos+=x+":";
			datos+=y;
			out.println(datos);
		}else throw new Exception("Debe esperar su turno");
	}
	/**
	 * llena los datos de la jugada para actualizar la interfaz.<br>
	 * @param fila del tablero de juego.
	 * @param columna del tablero de juego.
	 * @param imgX2 Imagen que se debe actualizar
	 */
	public void jugada(int fila, int columna, String img) {
		jugada=fila+":"+columna+":"+img;
	}
	/**
	 * Pasa el turno
	 */
	public void pasarTurno(){
		verTurno=!verTurno;
	}
	/**
	 * Devuelve el nombre del jugador
	 */
	public String darNombre(){
		return nombre;
	}
	/**
	 * Devuelve el turno
	 */
	public String darTurno(){
		return turno;
	}
	/**
	 * Devuelve los datos de la jugada
	 */
	public String darJugada(){
		return jugada;
	}
	/**
	 * Devuelve el estado del juego
	 */
	public String darEstado(){
		return estado;
	}
}
