package window;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import virtual_machine.MemoryException;
import virtual_machine.VirtualMachine;

public class MemoryGraphics {

	
	private Font theFont = Font.font("Courier New", FontWeight.BOLD, 11);
	
	private int displayStartIndex = 0;
	static final int DISPLAY_RANGE = 20;
	final long MEMORY_RANGE = 2147483647 + 1;
	
	public int radix = 16; //initial displays in hex
	private String hexFormatLong = "%08x";
	private String hexFormatShort = "%02x";
//	private String decFormatLong = "%010d";
	private String decFormatShort = "%03d";
	
	VirtualMachine _vm;
	private Map<Integer, List<TextField>> _memDisplays = new HashMap<>(DISPLAY_RANGE);
	private List<Text> _displayAddresses = new ArrayList<>(DISPLAY_RANGE);
	private int[] MASKS = {0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000};
	
	//Memory range is the range of possible addresses; to give the illusion of large memory, 
	// the range goes out to the largest value possible for an integer: 2^31 (exclusive). In reality,
	// to reflect byte-addressing, only about 1/4th of those addresses should be accessed. 
	
	private InOutErrorField ec;
	
	public MemoryGraphics(InOutErrorField console, VirtualMachine vm) {
		ec = console;
		_vm = vm;
		for (int i = 0; i < DISPLAY_RANGE; i++) {
			Text addDisplay = new Text(" " + String.format(hexFormatLong, i*4));
			addDisplay.setFont(theFont);
			_displayAddresses.add(addDisplay);
			ArrayList<TextField> list = new ArrayList<>();
			for (int j = 0; j < 4; j++) {
				TextField memIn = new TextField(String.format(hexFormatShort, 0)); //This should use getMemory from memory, not 0
				memIn.setFont(theFont);
				memIn.setPrefColumnCount(3);
				memIn.setAlignment(Pos.BASELINE_RIGHT);
				list.add(memIn);
			}
			_memDisplays.put(i, list);
		}
	}
	
//	public List<Integer> memoryRange(int start, int end) {
//		List<Integer> target = new ArrayList<>();
//		for (int i = start; i < end; i++) {
//			int val = _vm.getMainMem(i);
//			target.add(val);
//		}
//		return target;
//	}
	
	public void display(int pageAmt) {
//		updateMemoryFromDisplay();
		displayStartIndex += pageAmt;
		int address = displayStartIndex;
		int i = 0;
		int displayStop = address + (DISPLAY_RANGE * 4);
		while (address < displayStop) {
			String aDisp = String.format(hexFormatLong, address);
			_displayAddresses.get(i).setText(aDisp);
			int wordInt = 0;
			try {
				wordInt = _vm.getMainMem(address, 4);
			} catch (MemoryException e) {
				String errorMessage = e.getMessage() + "in graphics";
				ec.reportError(errorMessage, 0, null);
			}
			List<TextField> l = _memDisplays.get(i);
			int b = MASKS.length - 1;
			for (int j = 0; j < l.size(); j++) {
				int val = wordInt & MASKS[b];
				val = val >>> (b*8);
				String format;
				if (radix == 16) {
					format = hexFormatShort;
				} else {
					format = decFormatShort;
				}
				l.get(j).setText(String.format(format, val));
				b--;
			}
			i++;
			address += 4;
		}
	}
	
	public List<TextField> getValDisplays(int index) {
		return _memDisplays.get(index);
	}
	
	public Text getAddDisplay(int index) {
		return _displayAddresses.get(index);
	}
	
	public int getDisplayIndex() {
		return displayStartIndex;
	}
	
	public void setDisplayRadix(int newRad) {
		radix = newRad;
		display(0);
	}
	
	// returns true if the the memory changed
	//TODO: This might be an old version; please review carefully before putting this back into the program
	public void updateMemoryFromDisplay() {
//		for (int i = 0; i < DISPLAY_RANGE; i++) {
//			String toParse = "";
//			for (int j = 0; j < 4; j++) {
//				toParse += _memDisplays.get((i*4)+j).getText();
//			}
//			int value = Integer.parseInt(toParse, 16);
//			if (value != 0) {
//				String addressString = _displayAddresses.get(i).getText();
//				int address = Integer.parseInt(addressString, 16);
//				int old = _memory.getMemory(address);
//				if (old != value) {
//					_memory.putMemory(address, value); //this would need to be in try/catch
//				}
//			}
//		}

	}
	
	public void clear() {
		_vm.resetMainMem();
		for (int i = 0; i < DISPLAY_RANGE; i++) {
			List<TextField> l = _memDisplays.get(i);
			for (TextField t : l) {
				t.setText(String.format("%02x", 0));
				//if this were truly robust it would get the display values from memory
				//but we just cleared memory, so rather than waste time looking up values, just blanket set them to zero
			}
		}
		display(0);
	}

	public void go(String text) {
		if (text.equals("") || text.equals(" ")) {
			display(0);
		} else {
			int address = 0;
			try {
				address = Integer.parseInt(text, 16);
			} catch (NumberFormatException e) {
				ec.reportError("invalid goTo text: " + text, 0, null);
			}
			if (address == 0) {
				int pgAmt = 0 - displayStartIndex;
				display(pgAmt);
			} else {
				address = address - (address % 4);
				int pgAmt = address - displayStartIndex;
				display(pgAmt);
			}
		}
		ec.logErrors("Memory Display");
	}
	
}
