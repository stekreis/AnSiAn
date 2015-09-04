package de.tu.darmstadt.seemoo.ansian.tools.morse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Log;
import de.tu.darmstadt.seemoo.ansian.MainActivity;

public class Parser extends DefaultHandler {
	private List<MorseCodeCharacter> tempMorseCharacters;
	private MorseCodeCharacter tempMorseCharacter;

	private boolean letter, code;

	public Parser() {
		tempMorseCharacters = new ArrayList<MorseCodeCharacter>();

		letter = false;
		code = false;
	}

	public List<MorseCodeCharacter> parseDocument(String pathToDocument) {
		InputSource is = null;

		try {
			AssetManager assetManager = MainActivity.instance.getAssets();
			InputStream inputStream = assetManager.open(pathToDocument);
			Log.d("Parser", pathToDocument);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			is = new InputSource(reader);
			is.setEncoding("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		SAXParserFactory spf = SAXParserFactory.newInstance();

		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(is, this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tempMorseCharacters;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("morsecharacter")) {
			tempMorseCharacter = new MorseCodeCharacter();
		} else if (qName.equalsIgnoreCase("letter")) {
			letter = true;
		} else if (qName.equalsIgnoreCase("code")) {
			code = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (letter) {
			tempMorseCharacter.setLetter(new String(ch, start, length));
			letter = false;
		} else if (code) {
			tempMorseCharacter.setCode(new String(ch, start, length));
			code = false;
			tempMorseCharacters.add(tempMorseCharacter);
		}
	}
}