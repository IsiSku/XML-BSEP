package gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class KeypairTable extends JTable {

	private static final long serialVersionUID = 158730540208618426L;
	
	public KeypairTable() {
		DefaultTableModel dtm = new DefaultTableModel();
		dtm.addColumn("No.");
		dtm.addColumn("Alias");
		this.setModel(dtm);
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

}
