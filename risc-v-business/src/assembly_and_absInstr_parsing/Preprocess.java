package assembly_and_absInstr_parsing;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import util.Pair;


public class Preprocess {

	public static final char COMMENT_SYMBOL = '#';
	
	private static Pattern commentPattern =
			Pattern.compile(Character.toString(COMMENT_SYMBOL));
	private static Pattern labelMarkerPattern = Pattern.compile(":");
	private static Pattern labelPattern = Pattern.compile("[_\\p{Alpha}]\\w*");
	
	public static Pair<SymbolTable, List<Pair<String, Integer>>> preprocess(String source)
			throws ParseException {
		SymbolTable symbolTable = new SymbolTable();
		List<Pair<String, Integer>> codeLines = new ArrayList<Pair<String, Integer>>();
		String currentLabel = null;
		String[] lines = source.split("\n");
		for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
			String currentLine = lines[lineNumber];
			
			String[] commentSplit = commentPattern.split(currentLine);
			if (commentSplit.length > 1) {
				currentLine = commentSplit[0];
			}
			
			currentLine = currentLine.toLowerCase();
			
			currentLine += " ";  // TODO: fix this ugly hack
			
			String[] labelSplit = labelMarkerPattern.split(currentLine);
			if (labelSplit.length == 1) {
				// no label present, do nothing
			} else if (labelSplit.length == 2) {
				currentLabel = labelSplit[0];
				if (labelPattern.matcher(currentLabel).matches()) {
					Pair<Integer, Integer> labelLines =
							new Pair<Integer, Integer>(lineNumber, codeLines.size());
					// TODO: assure label is unique
					symbolTable.add(currentLabel, labelLines);
					currentLabel = null;
					currentLine = labelSplit[1];
				} else {
					throw new ParseException("invalid label", lineNumber);
				}
			} else {
				throw new ParseException("more than one : found", lineNumber);
			}
			
			currentLine = currentLine.trim();
			if (!currentLine.isEmpty()) {
				codeLines.add(new Pair<String, Integer>(currentLine, lineNumber));
			}
		}
		
		return new Pair<SymbolTable, List<Pair<String, Integer>>>(symbolTable, codeLines);
	}
	
}
