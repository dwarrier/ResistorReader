package com.example.chordedkeyboard;

import java.util.Hashtable;
import java.util.Map;

public class KeyMap {
	public static Hashtable<Integer, Character> mapping;

	static {
		mapping = new Hashtable<Integer, Character>();
		
		/** Single Finger Inputs */
		mapping.put(0x1000, 'n');
		mapping.put(0x0100, 'e');
		mapping.put(0x0010, 't');
		mapping.put(0x0001, 'a');
		
		/** Two Finger Inputs */
		mapping.put(0x1100, 'h');
		mapping.put(0x1010, 'r');
		mapping.put(0x1001, 'd');
		mapping.put(0x0110, 'i');
		mapping.put(0x0101, 's');
		mapping.put(0x0011, 'o');
		
		/** Sequential Single Inputs */
		mapping.put(0x0102, 'p');
		mapping.put(0x0201, 'l');
		mapping.put(0x1020, 'u');
		mapping.put(0x1002, 'c');
		mapping.put(0x2001, 'f');
		mapping.put(0x2010, 'm');
		
		/** Sequential Single Then Two Finger Inputs */
		mapping.put(0x1022, 'k');
		mapping.put(0x2021, 'w');
		mapping.put(0x1200, 'b');
		mapping.put(0x2201, 'g');
		mapping.put(0x2210, 'y');
		
		/** Sequential Multi Inputs */
		mapping.put(0x1110, 'z');
		mapping.put(0x1111, ' ');
		mapping.put(0x0120, 'x');
		mapping.put(0x1222, 'j');
		mapping.put(0x2100, 'q');
		mapping.put(0x2221, 'v');
		mapping.put(0x0111, (char) 127);
		mapping.put(0x1011, (char) 179);
	}
	
	public static char toChar(int[] inputState)
	{
		int value = toInt(inputState);
		
		if(!mapping.containsKey(value))
		{
			return '\0';
		}
		
		return mapping.get(value);
	}
	
	public static int toInt(int[] inputState)
	{
		return inputState[0] * 0x1000 + inputState[1] * 0x100
				+ inputState[2] * 0x10 + inputState[3];
	}
	
	public static int[] toButtons(int value)
	{
		int[] buttons = new int[4];

		buttons[0] = (value & 0x3000) >> 12;
		buttons[1] = (value & 0x300) >> 8;
		buttons[2] = (value & 0x30) >> 4;
		buttons[3] = (value & 0x3);
		
		return buttons;
	}
	
	public static String[] getHints(int input)
	{
		return getHints(toButtons(input));
	}
	
	public static String[] getHints(int[] downButtons)
	{
		boolean first = true;
		
		for(int i=0; i<4; i++)
		{
			if(downButtons[i] > 0)
			{
				first = false;
			}
		}

		String[] hints1 = new String[4];
		String[] hints2 = new String[4];
		
		for(int i=0; i<4; i++)
		{
			hints1[i] = "";
			hints2[i] = "";
		}
		
		for(Map.Entry<Integer, Character> entry : mapping.entrySet())
		{
			// Exclude delete and space from the hints.
			if(entry.getValue() == ' ' || entry.getValue() == (char) 127 || entry.getValue() == (char) 179)
			{
				continue;
			}
			
			//System.out.println(Integer.toHexString(entry.getKey()) + " " + entry.getValue());
			
			int[] charButtons = toButtons(entry.getKey());
			boolean match = true;
			boolean chorded = false;
			
			for(int i=0; i<4; i++)
			{
				if(charButtons[i] == 2)
				{
					chorded = true;
				}
			}

			//System.out.println(Integer.toHexString(entry.getKey()) + " " + entry.getValue());
			for(int i=0; i<4; i++)
			{
				if(downButtons[i] > 0 && charButtons[i] > 0 && downButtons[i] != charButtons[i])
				{
					match = false;
					break;
				}
				else if(!first && (charButtons[i] == 1 && downButtons[i] != 1) || (downButtons[i] == 1 && charButtons[i] != 1))
				{
					match = false;
					break;
				}
			}
			
			
			if(match)
			{
				System.out.println(first + " " + Integer.toHexString(entry.getKey()) + " '" + entry.getValue() + "'");
				System.out.println("Match");
				
				for(int i=0; i<4; i++)
				{
					if(charButtons[i] == 1 && chorded) {
						hints1[i] += entry.getValue();
					} else if(charButtons[i] == 1 || (charButtons[i] == 2 && !first)) {
						hints2[i] += entry.getValue();
					}
				}
			}
		}

		String[] hints = new String[4];
		
		for(int i=0; i<4; i++)
		{
			hints[i] = hints1[i] + " " + hints2[i];
			System.out.println(i + " " + hints1[i] + " " + hints2[i]);
		}
		
		return hints;
	}
}

