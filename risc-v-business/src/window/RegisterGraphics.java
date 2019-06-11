package window;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import virtual_machine.VirtualMachine;

public class RegisterGraphics {

	public static final int REGISTER_COUNT = 32;
	private Map<Integer, TextField> _regs = new HashMap<>();
	private Font rFont = Font.font("Courier New", FontWeight.BOLD, 11);
	private Font pcFont = Font.font("Courier New", FontWeight.BOLD, 12);
	private VirtualMachine _vm;
	
	public RegisterGraphics(InOutErrorField error, VirtualMachine vm) {
		_vm = vm;
		TextField zero = new TextField();
		zero.setText("0x00000000");
		zero.setFont(rFont);
		zero.setEditable(false);
		_regs.put(0, zero);
		for (int reg = 1; reg < REGISTER_COUNT; reg++) {
			TextField regInput = new TextField();
			regInput.setFont(rFont);
			regInput.setPrefColumnCount(12);
			regInput.setText(String.format("0x%08x", _vm.getReg(reg)));
			_regs.put(reg, regInput);
		}
		TextField pcView = new TextField();
		pcView.setText(String.format("0x%08x", _vm.getPC() + 1));
		pcView.setFont(pcFont);
		pcView.setEditable(false);
		_regs.put(REGISTER_COUNT, pcView);
	}
	
//	public int putRegister(int key, int value) {
//		if (key < REGISTER_COUNT && key > 0) {
//			TextField register = _regs.get(key);
//			register.setText(String.format("0x%08x", value));
//			return _regValues.put(key, value);
//		} else {
//			if (key == 0) {
//				ec.reportError("cannot write to r0");
//			} else if (key == REGISTER_COUNT) {
//				ec.reportError("please use putPC to edit the program counter");
//			} else {
//				ec.reportError("invalid register number: r" + key);
//			}
//			return -4;
//		}
//	}
	
//	public int getRegister(int key) {
//		if (key < REGISTER_COUNT && key >= 0) {
//			TextField register = _regs.get(key);
//			String textVal = register.getText();
//			int fieldVal = Integer.parseInt(textVal, 16);
//			if (fieldVal == _regValues.get(key)) {
//				return _regValues.get(key);
//			} else {
//				if (key != 0) {
//					_regValues.put(key, fieldVal);
//				}
//				return fieldVal;
//			}
//		} else {
//			if (key == REGISTER_COUNT) {
//				ec.reportError("please use getPC to get the program counter");
//			} else {
//				ec.reportError("invalid register number: r" + key);
//			}
//			//to do: figure out a way so that if error happens, no return is necessary
//			return -4;
//		}
//		
//	}
	
	TextField getField(int key) {
		return _regs.get(key);
	}
	
//	public void updateRegValues() {
//		for (int i = 0; i < REGISTER_COUNT; i++) {
//			TextField register = _regs.get(i);
//			String toParse = register.getText().substring(2);
//			int fieldVal = Integer.parseInt(toParse, 16);
//			int val = _regValues.get(i);
//			if (fieldVal != val && i != 0) {
//				if (fieldVal == 0) {
//					register.setText(String.format("0x%08x", val));
//				} else {
//					_regValues.put(i, fieldVal);
//				}
//			}
//		}
//		TextField pc = _regs.get(32);
//		pc.setText(String.format("0x%08x", _regValues.get(32)));
//	}
	
	public void updateFromDisplay() {
		for (int i = 0; i < 32; i++) {
			TextField register = _regs.get(i);
			String toParse = register.getText().substring(2);
			int fieldVal = Integer.parseUnsignedInt(toParse, 16); //or use parseInt();
			_vm.getVmRegFile().putRegister(i, fieldVal);
		}
	}
	
	public void updateDisplay() {
		for (int i = 0; i < 32; i++) {
			TextField register = _regs.get(i);
			String newDisplay = String.format("0x%08x", _vm.getReg(i));
			register.setText(newDisplay);
		}
		TextField pc = _regs.get(32);
		String newPC = String.format("0x%08x", _vm.getPC() + 1);
		pc.setText(newPC);
	}
	
	public int getPC() {
		return _vm.getPC();
	}
	
//	public int putPC(int val) {
//		TextField pc = _regs.get(32);
//		pc.setText(String.format("0x%08x", val));
//		return _regValues.put(32, val);
//	}
	
	public void reset() {
		_vm.resetReg();
		_vm.resetPC();
		updateDisplay();
	}
	
}