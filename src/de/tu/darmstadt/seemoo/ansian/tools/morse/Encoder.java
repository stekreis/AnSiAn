package de.tu.darmstadt.seemoo.ansian.tools.morse;

public class Encoder {
	private MorseCodeCharacterGetter morseCodeCharacterGetter;
	private StringBuffer morseEncodedTextBuffer;

	public Encoder() {
		morseCodeCharacterGetter = new MorseCodeCharacterGetter("morsecode.xml");
	}

	public String encode(String unencodedText) {
		if (unencodedText.isEmpty())
			return "";
		morseEncodedTextBuffer = new StringBuffer();
		String[] splittedUnencodedText = unencodedText.split(" ");

		for (int encodedWordsCounter = 0; encodedWordsCounter < splittedUnencodedText.length; encodedWordsCounter++) {
			String wordToEncode = splittedUnencodedText[encodedWordsCounter];

			if (wordToEncode.startsWith("[") && wordToEncode.endsWith("]")) {
				handleIncompletelySeparatedWords(wordToEncode);
			} else {
				handleCorrectlySeparatedWord(wordToEncode);
			}
		}

		return new String(morseEncodedTextBuffer);
	}

	public int findOccurencesOfSequenceInString(String needle, String haystack) {
		int occurenceCounter = 0;

		int currentOccurencePosition = haystack.indexOf(needle);
		while (currentOccurencePosition != -1) {
			occurenceCounter++;
			currentOccurencePosition = haystack.indexOf(needle, currentOccurencePosition + needle.length());
		}

		return occurenceCounter;
	}

	private void handleCorrectlySeparatedWord(String wordToEncode) {
		for (int charCounter = 0; charCounter < wordToEncode.length(); charCounter++) {
			String tempChar = wordToEncode.subSequence(charCounter, charCounter + 1).toString();
			// split character with space
			morseEncodedTextBuffer.append(morseCodeCharacterGetter.getCodeForLetter(tempChar));
			if(charCounter!=wordToEncode.length()-1) morseEncodedTextBuffer.append(" ");
		}
		
		// split word with /
		morseEncodedTextBuffer.append("/");
	}

	private void handleIncompletelySeparatedWords(String wordToEncode) {
		if (findOccurencesOfSequenceInString("]", wordToEncode) == findOccurencesOfSequenceInString("[",
				wordToEncode)) {
			String[] splittedUnencodedText = wordToEncode.split("]");
			for (int amountOfSpecialWordsEncoded = 0; amountOfSpecialWordsEncoded < findOccurencesOfSequenceInString(
					"]", wordToEncode); amountOfSpecialWordsEncoded++) {
				morseEncodedTextBuffer.append(morseCodeCharacterGetter
						.getCodeForLetter(splittedUnencodedText[amountOfSpecialWordsEncoded] + "]"));
			}
		} else {
			handleCorrectlySeparatedWord(wordToEncode);
		}
	}
}