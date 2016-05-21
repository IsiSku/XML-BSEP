package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import data.IssuerData;
import data.SubjectData;
import net.miginfocom.swing.MigLayout;
import security.CertificateUtils;
import security.KeyStoreUtils;

/**
 * Main view of the application, extension of the {@link JFrame} class.
 * This window shows a keystore file with all it's certificates.
 * User can create new keystores and add certificates to it either by creating them or by importing them from file.
 * Certificates and keystores can then be exported/saved to file.
 * Only one keystore is showed at a time.
 * 
 */
public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1198734643308937757L;
	
	private static MainWindow instance = null;
	private KeyStore currentKeystore = null;
	private String currentPath = "";
	
	private KeypairTable keypairTable;
	
	private JPanel contentPane;
	private final Action actKeystore = new ActionKeystore();
	private final Action actKeypair = new ActionKeypair();
	private final Action actOpen = new ActionOpen();
	private final Action actSave = new ActionSave();
	private final Action actSaveAs = new ActionSaveAs();
	private final Action actExit = new ActionExit();
	private final Action actExportCertificate = new ActionExportCertificate();
	private final Action actExportAll = new ActionExportAll();
	private JTextField txtCurrentKeystore;
	private JLabel lblCurrentKeystore = new JLabel("Current keystore:");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Implementation of Singleton pattern.
	 * Returns the instance of the {@code MainWindow}.
	 * 
	 * @return {@link MainWindow}
	 */
	public static MainWindow getInstance() {
		if(instance == null) {
	         instance = new MainWindow();
	      }
	      return instance;
	}

	/**
	 * Create the frame.
	 */
	protected MainWindow() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		setTitle("CerGen");
		
		// Exit prompt.
		
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				String ObjButtons[] = {"Yes","No"};
		        int PromptResult = 
		        		JOptionPane.showOptionDialog(null,
		        									"Are you sure you want to exit?",
		        									"Leaving CerGen",
		        									JOptionPane.DEFAULT_OPTION,
		        									JOptionPane.WARNING_MESSAGE,
		        									null,
		        									ObjButtons,
		        									ObjButtons[1]);
		        if(PromptResult == JOptionPane.YES_OPTION)
		        {
		            System.exit(0);
		        }
			}
			
		});
		
		// Components.
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenu mntmNew = new JMenu("New");
		mnFile.add(mntmNew);
		
		JMenuItem mntmKeystore = mntmNew.add(actKeystore);
		mntmKeystore.setText("Keystore");
		
		JMenuItem mntmKeypair = mntmNew.add(actKeypair);
		mntmKeypair.setText("Keypair");
		
		JMenuItem mntmOpen = mnFile.add(actOpen);
		mntmOpen.setText("Open");
		
		JSeparator sepFile1 = new JSeparator();
		mnFile.add(sepFile1);
		
		JMenuItem mntmSave = mnFile.add(actSave);
		mntmSave.setText("Save");
		
		JMenuItem mntmSaveAs = mnFile.add(actSaveAs);
		mntmSaveAs.setText("Save as...");
		
		JSeparator sepFile2 = new JSeparator();
		mnFile.add(sepFile2);
		
		JMenuItem mntmExit = mnFile.add(actExit);
		mntmExit.setText("Exit");
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		
		JMenuItem mntmExportCertificate = mnTools.add(actExportCertificate);
		mntmExportCertificate.setText("Export Certificate");
		
		JMenuItem mntmExportAll = mnTools.add(actExportAll);
		mntmExportAll.setText("Export All");
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		keypairTable = new KeypairTable();
		keypairTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(keypairTable);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new MigLayout("", "[100px][grow,fill]", "[22px]"));
		
		lblCurrentKeystore = new JLabel("Current keystore:");
		panel.add(lblCurrentKeystore, "cell 0 0,alignx trailing,aligny center");
		
		txtCurrentKeystore = new JTextField();
		txtCurrentKeystore.setEditable(false);
		panel.add(txtCurrentKeystore, "cell 1 0,growx");
		txtCurrentKeystore.setColumns(10);
		txtCurrentKeystore.setText("");
	}
	
// ---------------------------------------------------------------------------------------------------
// Actions section because I couldn't be bothered to move them to separate files.
// ---------------------------------------------------------------------------------------------------
	
	
// Keystore stuff ------------------------------------------------------------------------------------
	
	/**
	 * Creates a new keystore.
	 * Opens a dialog for defining keystore password.
	 */
	private class ActionKeystore extends AbstractAction {
		private static final long serialVersionUID = 425412543121784713L;
		public ActionKeystore() {
			putValue(NAME, "Keystore");
			putValue(SHORT_DESCRIPTION, "Create new keystore");
		}
		public void actionPerformed(ActionEvent e) {
			// Placeholder password. Will be set when keystore is save to file.			
			currentKeystore = KeyStoreUtils.loadKeyStore(null, "placeholder".toCharArray());
			txtCurrentKeystore.setText("New keystore");
			lblCurrentKeystore.setText("*Current keystore:");
		}
	}
	
	/**
	 * Saves the current keystore into file.
	 * If it's a new keystore, {@link ActionSaveAs} is called.
	 *
	 */
	private class ActionSave extends AbstractAction {
		private static final long serialVersionUID = -4641089031850059072L;
		public ActionSave() {
			putValue(NAME, "Save");
			putValue(SHORT_DESCRIPTION, "Save keystore");
		}
		public void actionPerformed(ActionEvent e) {
			// Is there a keystore at all?
			if(currentKeystore == null) {
				JOptionPane.showMessageDialog(MainWindow.getInstance(),
						"There is no active keystore to be saved. Please create or open a keystore first.");
				return;
			}
			
			// This is a new keystore - call SaveAs.
			if(currentPath.equals("")) {
				actSaveAs.actionPerformed(null);
				return;
			}
			else {	// Existing. Overwrite the file.
				// Set password for keystore.
		    	KeystoreDialog ksd = new KeystoreDialog();
				ksd.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
				ksd.setVisible(true);
				
				// After returning from the modal dialog.					
				KeyStoreUtils.saveKeyStore(currentKeystore, currentPath, ksd.getPassword());
				txtCurrentKeystore.setText(currentPath);
				lblCurrentKeystore.setText("Current keystore:");
			}
		}
	}
	
	/**
	 * Opens a file choosing dialog to select the path where the keystore will be saved.
	 * Default file name is untitled.
	 *
	 */
	private class ActionSaveAs extends AbstractAction {
		private static final long serialVersionUID = 3925404848236570471L;
		public ActionSaveAs() {
			putValue(NAME, "Save As");
			putValue(SHORT_DESCRIPTION, "Save keystore to a file");
		}
		public void actionPerformed(ActionEvent e) {			
			// Is there a keystore at all?
			if(currentKeystore == null) {
				JOptionPane.showMessageDialog(MainWindow.getInstance(),
						"There is no active keystore to be saved. Please create or open a keystore first.");
				return;
			}
			
			// Set default file chooser directory. Create the dialog.
			String workingDir = System.getProperty("user.dir");
			workingDir = Paths.get(workingDir, "certificates").toString();
			JFileChooser chooser = new JFileChooser(workingDir);
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("Java keystore files", "jks");
		    chooser.setFileFilter(filter);
		    
		    String path = "";
		    		    
		    // User gave up.
		    int returnVal = chooser.showSaveDialog(MainWindow.getInstance());
		    if (returnVal == JFileChooser.CANCEL_OPTION) {
		    	return;
		    }
		    
		    // User approved.
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		    	// Check if user forgot file extension or got it wrong.
		        path = chooser.getSelectedFile().getAbsolutePath();
		        if(!path.endsWith(".jks")) {
		        	path = path.concat(".jks");
		        }
		        
		        //TODO: A:Important: Check if filename is "".
		        
		        // Does this file already exist? Overwrite it?
			    File f = new File(path);
			    if(f.isFile()) {
			    	String ObjButtons[] = {"Yes","No"};
			        int PromptResult = 
			        		JOptionPane.showOptionDialog(null,
			        									"Selected file already exists, would you like to overwrite existing file?",
			        									"Overwrite existing file",
			        									JOptionPane.DEFAULT_OPTION,
			        									JOptionPane.WARNING_MESSAGE,
			        									null,
			        									ObjButtons,
			        									ObjButtons[1]);
			        // Yep, overwrite.
			        if(PromptResult == JOptionPane.YES_OPTION)
			        {
			        	// Set password for keystore.
				    	KeystoreDialog ksd = new KeystoreDialog();
						ksd.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
						ksd.setVisible(true);
						
						// After returning from the modal dialog.					
						KeyStoreUtils.saveKeyStore(currentKeystore, path, ksd.getPassword());
				    	currentPath = path;
				    	txtCurrentKeystore.setText(currentPath);
				    	lblCurrentKeystore.setText("Current keystore:");
				    	// Clean up.
				    	Arrays.fill(ksd.getPassword(), '0');
						ksd.dispose();
			        }
			        
			    }
			    else {
			    	// Set password for keystore.
			    	KeystoreDialog ksd = new KeystoreDialog();
					ksd.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
					ksd.setVisible(true);
					
					// After returning from the modal dialog.					
					KeyStoreUtils.saveKeyStore(currentKeystore, path, ksd.getPassword());
			    	currentPath = path;
			    	txtCurrentKeystore.setText(currentPath);
			    	lblCurrentKeystore.setText("Current keystore:");
			    	// Clean up.
			    	Arrays.fill(ksd.getPassword(), '0');
					ksd.dispose();
			    }
		    }
		    
		    
		}
	}
	
	private class ActionOpen extends AbstractAction {
		private static final long serialVersionUID = 340823143919984037L;
		public ActionOpen() {
			putValue(NAME, "Open");
			putValue(SHORT_DESCRIPTION, "Open a keystore file");
		}
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(MainWindow.getInstance(), "Coming soon.");
			// TODO: Open keystore file.
		}
	}
	
// Certificate stuff --------------------------------------------------------------------------------------
	
	/**
	 * Generates a keypair.
	 * Prompts the user to fill in certificate details and generates the certificate.
	 */
	private class ActionKeypair extends AbstractAction {
		private static final long serialVersionUID = -1411136323257319945L;
		public ActionKeypair() {
			putValue(NAME, "Keypair");
			putValue(SHORT_DESCRIPTION, "Generate new keypair");
		}
		public void actionPerformed(ActionEvent e) {
			// Is there a KeyStore to which this KeyPair will be stored?
			if(currentKeystore == null) {
				JOptionPane.showMessageDialog(MainWindow.getInstance(),
						"A keystore is needed to create a keypair. Please create a keystore first. "
						+ "You can do this by going to the File menu, selecting New and then Keystore");
				return;
			}
			
			KeyPair kp = CertificateUtils.generateKeyPair();
			IssuerData issuer = new IssuerData();
			SubjectData subject = new SubjectData();
			
			CertificateDialog cd = new CertificateDialog(issuer, subject, kp);
			cd.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			cd.setVisible(true);
			
			// After returning from the modal dialog.
			if (cd.getCertificate() != null) {
				try {
					currentKeystore.setCertificateEntry(cd.getAlias(), cd.getCertificate());
					
					KeyStoreUtils.write(currentKeystore, cd.getAlias(), kp.getPrivate(), cd.getPassword(), cd.getCertificate());

					// Clean up password.
					Arrays.fill(cd.getPassword(), '0');
					
					// Update view.
					int rows = ((DefaultTableModel)keypairTable.getModel()).getRowCount();
					((DefaultTableModel)keypairTable.getModel()).addRow(new Object[]{rows+1, cd.getAlias()});
					
			    	lblCurrentKeystore.setText("*Current keystore:");
					
				} catch (KeyStoreException e1) {
					e1.printStackTrace();
				}
			}
			cd.dispose();
		}
	}
	
	
	
	
	
	/**
	 * Sends the window closing event, and triggers appropriate listeners.
	 */
	private class ActionExit extends AbstractAction {
		private static final long serialVersionUID = 2732771330480399657L;
		public ActionExit() {
			putValue(NAME, "Exit");
			putValue(SHORT_DESCRIPTION, "Close the application");
		}
		public void actionPerformed(ActionEvent e) {			
			MainWindow.getInstance().dispatchEvent(new WindowEvent(MainWindow.getInstance(), WindowEvent.WINDOW_CLOSING));
		}
	}
	private class ActionExportCertificate extends AbstractAction {
		private static final long serialVersionUID = -1698079888963949279L;
		public ActionExportCertificate() {
			putValue(NAME, "Export Certificate");
			putValue(SHORT_DESCRIPTION, "Export keypair to a certificate file");
		}
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(MainWindow.getInstance(), "Coming soon.");
			// TODO: Export certificate.
			// TODO: Import certificate.
		}
	}
	private class ActionExportAll extends AbstractAction {
		private static final long serialVersionUID = 5683267289392412616L;
		public ActionExportAll() {
			putValue(NAME, "Export All");
			putValue(SHORT_DESCRIPTION, "Export all certificates to a specified folder");
		}
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(MainWindow.getInstance(), "Coming soon.");
			// TODO: Export all certificates.
		}
	}
}
