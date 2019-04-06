package InterfazCliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import Cliente.TriquiTraque;

public class InterfazCliente extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Representa una conexión con el cliente
	 */
	private TriquiTraque cliente;
	//Atributos de la clase
	private JButton[][] botones;
	ActionListener act;
	private JTextArea txtArea;
	private static final String INICIAR_JUEGO="Iniciar";
	private static final String SALIR="Salir";
	private static final String OPCION1="Opcion1";
	private static final String OPCION2="Opcion2";
	private JMenuBar menuBar;
	private JMenu menuInicio;
	private JMenuItem itemIniciar;
	private JMenuItem itemSalir;
	private JMenu menuOpcion;
	private JMenuItem itemOpcion1;
	private JMenuItem itemOpcion2;
	
	//Elementos del dialogo
	private JDialog dialogo;
	private static final String CONECTAR="Conectar";
	private JLabel etiquetaNombre, etiquetaServidor, etiquetaPuerto;
	private JTextField txtNombre, txtServidor, txtPuerto;
	private JButton btnConectar;
	private String nCliente;

	/**
	 * Constructor de la clase
	 */
	public InterfazCliente(){
		setLayout(new BorderLayout());
		setTitle("Tu turno: ");
		setSize(500, 500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		menuBar=new JMenuBar();
		this.setJMenuBar(menuBar);

		menuInicio=new JMenu("Inicio");
		menuBar.add(menuInicio);
		itemIniciar = new JMenuItem( "Iniciar Juego" );
		itemIniciar.setActionCommand( INICIAR_JUEGO );
		itemIniciar.addActionListener( this );
		menuInicio.add( itemIniciar );
		itemSalir = new JMenuItem( "Salir" );
		itemSalir.setActionCommand( SALIR );
		itemSalir.addActionListener( this );
		menuInicio.add( itemSalir );

		menuOpcion = new JMenu( "Extensión" );
		menuBar.add( menuOpcion );
		itemOpcion1 = new JMenuItem( "Opción 1" );
		itemOpcion1.setActionCommand( OPCION1 );
		itemOpcion1.addActionListener( this );
		menuOpcion.add( itemOpcion1 );
		itemOpcion2 = new JMenuItem( "Opción 2" );
		itemOpcion2.setActionCommand( OPCION2 );
		itemOpcion2.addActionListener( this );
		menuOpcion.add( itemOpcion2 );

		//Crea el panel de la imagen principal
		JPanel PanelImagen = new JPanel();
		PanelImagen.setBorder(new TitledBorder(""));
		JLabel imagen = new JLabel("");
		ImageIcon icono=new ImageIcon("./data/Titulo.jpg");
		imagen.setIcon(icono);
		PanelImagen.add(imagen);
		//Crea el panel con los botones del tablero
		JPanel tablero=new JPanel();
		tablero.setBorder(new TitledBorder("Tablero"));
		tablero.setLayout(new GridLayout(3, 3));
		botones=new JButton[3][3];
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				JButton boton=new JButton();
				boton.setBorder(new LineBorder(Color.BLACK));
				boton.setActionCommand(i+":"+j);
				boton.addActionListener(act=new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String comando=e.getActionCommand();
						if(cliente!=null){
							String[] coord=comando.split(":");
							int fila=Integer.parseInt(coord[0]);
							int columna=Integer.parseInt(coord[1]);
							if(fila>=0 && columna>=0){
								try {
									cliente.enviarTurno(fila, columna);
									actualizarTablero();
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null, ex.getMessage(), "Triqui Traque", JOptionPane.INFORMATION_MESSAGE);
								}
							}
						}else JOptionPane.showMessageDialog(null, "Debe iniciar el juego", "Triqui Traque", JOptionPane.ERROR_MESSAGE);
					}
				});
				boton.setSize(10, 10);
				botones[i][j]=boton;
				tablero.add(botones[i][j]);
			}
		}
		//Panel mensajes
		JPanel PanelMensajes=new JPanel();
		PanelMensajes.setBorder(new TitledBorder("Mensajes del sistema"));
		JScrollPane scroll = new JScrollPane( );
		scroll.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		txtArea = new JTextArea( 6, 40 );
		txtArea.setWrapStyleWord( true );
		txtArea.setLineWrap( true );
		txtArea.setEditable( false );
		scroll.getViewport( ).add( txtArea );
		PanelMensajes.add(scroll);

		add(PanelImagen, BorderLayout.NORTH);
		add(tablero, BorderLayout.CENTER);
		add(PanelMensajes, BorderLayout.SOUTH);
	}
	/**
	 * Actualizar el tablero
	 */
	public void actualizarTablero(){
		String inf=cliente.darJugada();
		String[] cadena=inf.split(":");
		int fila=Integer.valueOf(cadena[0]);
		int columna=Integer.valueOf(cadena[1]);
		String img=cadena[2];
		botones[fila][columna].setIcon(new ImageIcon(img));
		botones[fila][columna].removeActionListener(act);
		cliente.pasarTurno();
	}
	/**
	 * Termina el juego y muestra el ganador
	 */
	public void terminarJuego(){
		String estado=cliente.darEstado();
		if(estado.equals(TriquiTraque.GANASTE)){
			JOptionPane.showMessageDialog(this, "Ganaste");
			new InterfazCliente().setVisible(true);
			this.dispose();
		}else if(estado.equals(TriquiTraque.EMPATE)){
			JOptionPane.showMessageDialog(this, "Empate");
			new InterfazCliente().setVisible(true);
			this.dispose();
		}else if(estado.equals(TriquiTraque.PERDISTE)){
			JOptionPane.showMessageDialog(this, "Perdiste");
			new InterfazCliente().setVisible(true);
			this.dispose();
		}
	}
	/**
	 * Agrega todos los mensajes de la colección al campo de texto, uno por uno
	 * @param mensajes Es una lista de mensajes que deben mostrarse
	 */
	public void agregarMensajes( Collection mensajes )
	{
		Iterator iter = mensajes.iterator( );
		while( iter.hasNext( ) )
		{
			String mensaje = ( String )iter.next( );
			txtArea.append( mensaje + "\n" );
			txtArea.setCaretPosition( txtArea.getText( ).length( ) );
		}
	}

	/**
	 * Genera un dialogo que captura los datos del jugador y crea un cliente.<br>
	 */
	public void mostrarDialogo(){
		dialogo=new JDialog();
		dialogo.setTitle( "Datos de la Conexión" );
		dialogo.setSize( 400, 200 );
		dialogo.setVisible(true);
		
		//Panel de los datos
		JPanel datos=new JPanel();
		datos.setLayout(new GridBagLayout());
		//Etiquetas
		GridBagConstraints gbc = new GridBagConstraints( 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 );
		etiquetaNombre = new JLabel( "Nombre del Jugador:" );
		datos.add( etiquetaNombre, gbc );
		gbc = new GridBagConstraints( 0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 );
		etiquetaServidor = new JLabel( "Dirección Servidor:" );
		datos.add( etiquetaServidor, gbc );
		gbc = new GridBagConstraints( 0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 5, 5, 5 ), 0, 0 );
		etiquetaPuerto = new JLabel( "Puerto:" );
		datos.add( etiquetaPuerto, gbc );
		//Cajas de texto
		gbc = new GridBagConstraints( 1, 0, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 );
		txtNombre = new JTextField("DrackoDG");
		datos.add( txtNombre, gbc );
		gbc = new GridBagConstraints( 1, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 );
		txtServidor = new JTextField("localhost");
		datos.add( txtServidor, gbc );
		gbc = new GridBagConstraints( 1, 2, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 );
		txtPuerto = new JTextField("9999");
		datos.add( txtPuerto, gbc );
		// Botones
		gbc = new GridBagConstraints( 1, 3, 1, 1, 0.5, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 );
		btnConectar = new JButton( "Conectar" );
		btnConectar.setActionCommand( CONECTAR );
		btnConectar.addActionListener( this );
		datos.add( btnConectar, gbc );

		dialogo.getContentPane().add(datos);
	}
	/**
	 * Actualiza el turno que le toca al jugador
	 */
	public void actualizarTurno(){
		setTitle("Tu turno: "+cliente.darTurno());
	}
	/**
	 * Captura los eventos de los botones
	 */
	public void actionPerformed(ActionEvent evento) {
		String comando=evento.getActionCommand();
		if(comando.equals(SALIR)) this.dispose();
		else if(comando.equals(INICIAR_JUEGO)){
			mostrarDialogo();
		}else if(comando.equals(OPCION1)){
			JOptionPane.showMessageDialog(this, "Opcion 1");
		}else if(comando.equals(OPCION2)){
			JOptionPane.showMessageDialog(this, "Opcion 2");
		}else if(comando.equals(CONECTAR)){
			nCliente=txtNombre.getText();
			String servidor=txtServidor.getText();
			int puerto=Integer.parseInt(txtPuerto.getText());
			try{
				cliente = new TriquiTraque(puerto, servidor);
                Thread hilo = new Thread(cliente);
                hilo.start();
                dialogo.dispose();
				actualizarTurno();
			}catch(Exception e){
				JOptionPane.showMessageDialog(this, e.getMessage(), "Triqui Traque", JOptionPane.ERROR_MESSAGE);
			}
		}else{
			}
	}
	/**
	 * Devuelve el nombre del cliente
	 */
	public String nomCliente(){
		return nCliente;
	}
	/**
	 * Crea la ventana
	 * @param args
	 */
	public static void main(String[] args) {
		InterfazCliente ventana=new InterfazCliente();
		ventana.setVisible(true);
	}
}
