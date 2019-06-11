package window;

import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class SymbolTable {
	
	public class SymbolEntry {
		public final SimpleStringProperty symbol;
		public final SimpleIntegerProperty line;
		private String symb;
		private int lin;
		
		SymbolEntry(int line, String sym) {
			this.symbol = new SimpleStringProperty(sym);
			this.line = new SimpleIntegerProperty(line);
			symb = sym;
			lin = line;
		}
		
		public SimpleStringProperty symbolProperty() {
			return this.symbol;
		}
		
		public SimpleIntegerProperty lineProperty() {
			return this.line;
		}
		
		public String getSymbol() {
			return symb;
		}
		
		public int getLine() {
			return lin;
		}
		
		public void setSymbol(String newValue) {
			symbol.set(newValue);
			symb = newValue;
		}
		
		public void setLine(int newValue) {
			line.set(newValue);
			lin = newValue;
		}
		
		@Override
		public boolean equals(Object o) {
			SymbolEntry other = (SymbolEntry)o;
			return (this.lin == other.getLine());
		}
		
		@Override
		public String toString() {
			return "<" + symb + "," + lin + ">";
		}
	}

	TableView<SymbolEntry> _table = new TableView<SymbolEntry>();
	private ObservableList<SymbolEntry> _data = FXCollections.observableArrayList();
	
	SymbolTable(EventHandler<MouseEvent> clicker) {
		
		_table.setEditable(true);
		_table.setPrefWidth(175);
		_table.setPrefHeight(350);
		TableColumn<SymbolEntry, String> symbols = new TableColumn<SymbolEntry, String>("Symbol");
		symbols.setMinWidth(100);
		symbols.setCellValueFactory(
				new PropertyValueFactory<SymbolEntry, String>("symbol"));
		symbols.setCellFactory(new CustomCellFactory<String>(clicker));
		TableColumn<SymbolEntry, Integer> lines = new TableColumn<SymbolEntry, Integer>("Line");
		lines.setMinWidth(75);
		lines.setCellValueFactory(
				new PropertyValueFactory<SymbolEntry,Integer>("line"));
		
		_table.setItems(_data);

		_table.getColumns().add(symbols);
		_table.getColumns().add(lines);
		_table.getSortOrder().add(lines);
	}
	
	public void updateTable(assembly_and_absInstr_parsing.SymbolTable table) {
		Set<SymbolEntry> testSet = new HashSet<SymbolEntry>();
		Set<String> tableEntries = table.getLabels();
		if (tableEntries != null && !tableEntries.isEmpty()) {
			for (String label : tableEntries) {
				testSet.add(new SymbolEntry(table.getSourceLine(label)+1, label));
			}
		}
		_data.clear();
		if (!testSet.isEmpty()) {
			for (SymbolEntry test : testSet) {
				_data.add(test);
			}
		}
		_table.sort();
	}
	
	public void clear() {
		_data.clear();
	}
	
//	private interface Highlighter {
//		public void colorLine(int i);
//	}
	
	class CustomCellFactory<T> implements Callback<TableColumn<SymbolEntry,T>, TableCell<SymbolEntry,T>> {
		EventHandler<MouseEvent> click;
		
		public CustomCellFactory(EventHandler<MouseEvent> click) {
			this.click = click;
		}
		
		public final TableCell<SymbolEntry,T> call(TableColumn<SymbolEntry,T> p) {
			TableCell<SymbolEntry, T> cell = new TableCell<SymbolEntry, T>() {
	               @Override
	               protected void updateItem(T item, boolean empty) {
	                  // calling super here is very important - don't skip this!
	                  super.updateItem(item, empty);
	                  if(item != null) {
	                      setText(""+item);
	                  }
	               }
			};
			if(click != null) {
		         cell.setOnMouseClicked(click);
		    }
			return cell;
		}
	}
}