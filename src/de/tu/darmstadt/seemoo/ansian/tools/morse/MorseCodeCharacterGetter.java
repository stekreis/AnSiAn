package de.tu.darmstadt.seemoo.ansian.tools.morse;

import java.util.List;

public class MorseCodeCharacterGetter {
	private List<MorseCodeCharacter> morseCodeCharacters;
	public static String ESCAPE_START = "<";
	public static String ESCAPE_END = ">";
	private boolean escaping = false;
	private StringBuilder escapeStringBuilder = new StringBuilder();

	public MorseCodeCharacterGetter(String pathToMorsecodeXML) {
		morseCodeCharacters = new Parser().parseDocument(pathToMorsecodeXML);
	}

	public String getCodeForLetter(String letter) {
		// if (letter.equals(escapeStart)){
		// escaping =true;
		// }
		//
		// if(escaping){
		// escapeStringBuilder.append(letter);
		// }
		//
		//
		// if (letter.equals(escapeEnd)){
		// escaping =false;
		// String temp = escapeStringBuilder.toString();
		// escapeStringBuilder = new StringBuilder();
		// return temp;
		// }
		//
		// if(escaping){return "";}

		for (MorseCodeCharacter mc : morseCodeCharacters) {
			if (mc.getLetter().equalsIgnoreCase(letter)) {
				return mc.getCode();
			}
		}

		return ESCAPE_START + letter + ESCAPE_END;
	}

	public String getLetterForCode(String code) {
		for (MorseCodeCharacter mc : morseCodeCharacters) {
			if (mc.getCode().equalsIgnoreCase(code)) {
				return mc.getLetter();
			}
		}
		if (code.contains(ESCAPE_END) || code.contains(ESCAPE_START))
			return getLetterForCode(removeEscapes(code));

		return ESCAPE_START + code + ESCAPE_END;
	}

	private String removeEscapes(String code) {
		code = code.replace(ESCAPE_START, "");
		code = code.replace(ESCAPE_END, "");
		return code;
	}
}