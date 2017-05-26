package jaz;

import java.util.Objects;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public final class Table {
	private final JTable _table;

	Table(JTable table) {
		_table = table;
	}

	public Result row(Object... columns) {
		Objects.requireNonNull(columns);

		new Exception().printStackTrace();

		SwingUtilities.invokeLater(() -> {
			_table.setTableHeader(null);

			DefaultTableModel model = (DefaultTableModel) _table.getModel();
			model.setColumnCount(columns.length);
			model.addRow(columns);

			_table.scrollRectToVisible(_table.getCellRect(_table.getRowCount() - 1, 0, true));
		});
		return new Result();
	}
}
