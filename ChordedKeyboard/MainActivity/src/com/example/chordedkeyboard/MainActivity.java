package com.example.chordedkeyboard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
	int[] buttonID = { R.id.button1, R.id.button2, R.id.button3, R.id.button4 };
	long originalTime = 0;
	int[] buttonStates = new int[4];
	final int DELAY = 50;
	int currentState;
	int[] charOut = new int[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		View v = findViewById(R.id.linearLayout2);
		MyOnTouchListener listener = new MyOnTouchListener();
		listener.outputView = (TextView) findViewById(R.id.editText2);
		v.setOnTouchListener(listener);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class MyOnTouchListener implements View.OnTouchListener {
		TextView outputView;
		int currentBox = 0;
		boolean done; // indicates that input has finished
		boolean skipGreens = false;
		boolean lastHit = false;
		//boolean comboSetUp = false;
		//boolean twoFingerCombo = false;
		int[] heldState = new int[4]; // indicates that a pre-input chord is
										// being held
		StringBuilder output = new StringBuilder();

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) { // first finger
																// down
				System.out.println("ACTION_DOWN " + event.getPointerCount());
				int buttonIndex = findButton(event.getX(), event.getY());
				currentBox = buttonIndex;
				buttonStates[buttonIndex] = 1;
				charOut[buttonIndex] = 1;
				updateButtons();
				originalTime = event.getEventTime();
				heldState = charOut;
				lastHit = true;
			} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
				System.out.println("ACTION_POINTER_DOWN "
						+ event.getPointerCount());

				if (event.getEventTime() - originalTime < DELAY) {
					System.out.println("TRUE");

					int pointerCount = event.getPointerCount();
					if (event.getPointerCount() == 2) {
						//comboSetUp = true;
					}
					for (int p = 0; p < pointerCount; p++) {
						int buttonIndex = findButton(event.getX(p),
								event.getY(p));

						if (buttonStates[buttonIndex] == 0) {
							System.out.println("button " + buttonIndex);
							buttonStates[buttonIndex] = 1;
							charOut[buttonIndex] = 1;
						}
					}
					if (event.getPointerCount() > 1) {
						lastHit = false;
					} else {
						lastHit = true;
					}
					heldState = charOut;
				} else {
					System.out.println("FALSE");

					int pointerCount = event.getPointerCount();
					for (int p = 0; p < pointerCount; p++) {
						int buttonIndex = findButton(event.getX(p),
								event.getY(p));

						if (buttonStates[buttonIndex] == 0) {
							System.out.println("button " + buttonIndex);
							buttonStates[buttonIndex] = 2;
							charOut[buttonIndex] = 2;
						}
					}
					lastHit = false;
					
					/*if (event.getPointerCount() == 3 && comboSetUp) {
						System.out.println("DSFDS");
						twoFingerCombo = true;
						comboSetUp = false;
					}*/
				}


				updateButtons();

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				System.out.println("ACTION_UP " + event.getPointerCount());
				//twoFingerCombo = false;
				int buttonIndex = findButton(event.getX(), event.getY());
				if (currentBox == buttonIndex) { //if still in same box as start of action
				}
				if (buttonStates[buttonIndex] == 1) {
					System.out.println(charOut[0] + " " + charOut[1] + " "
							+ charOut[2] + " " + charOut[3]);
					if (lastHit == true) {
						char out = KeyMap.toChar(charOut); // PRINT
						output.append(out);
						outputView.setText(output.toString());
					}
				}
				buttonStates[buttonIndex] = 0;
				charOut = new int[4];
				updateButtons();
				skipGreens = false;
			} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP) {
				System.out.println("ACTION_POINTER_UP "
						+ event.getPointerCount());

				for (int buttonIndex = 0; buttonIndex < buttonStates.length; buttonIndex++) {
					if (buttonStates[buttonIndex] > 0) {
						if ((buttonStates[buttonIndex] == 2 || buttonStates[buttonIndex] == 1)) {
							if (!skipGreens) {
								char out = KeyMap.toChar(charOut); // PRINT
								String outputStr = outputView.getText().toString();
								if ((out == (char) 127) && outputStr.length() > 0) { // DEL character
									output.deleteCharAt(output.length() - 1);
								} 			
								else if ((out == (char) 179) && outputStr.length() > 0) { //DEL word
									for (int i = outputStr.length() -1; i >= 0; i--) {
										if (Character.toString(outputStr.charAt(i)).equals(" ") || i == 0) {
												output.delete(i, output.length());
												break;
										}
									}
								}else {
									output.append(out);
								}
								outputView.setText(output.toString());
								buttonStates = heldState;
								skipGreens = true;
							}
							if (event.getPointerCount() == 2) {// || (event.getPointerCount() == 3 && twoFingerCombo)) {
								System.out.println("HERE");
								skipGreens = false;
							}
							break;
						} else {
							charOut[buttonIndex] = buttonStates[buttonIndex];
							buttonStates[buttonIndex] = 3;
						}
					}
				}

				int p = event.getActionIndex();
				int buttonIndex = findButton(event.getX(p), event.getY(p));
				buttonStates[buttonIndex] = 0;

				updateButtons();
			}
			else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				//outputView.setText(text)
			}
			return true;
		}

		private void updateButtons() {
			int buttonValue = KeyMap.toInt(buttonStates);
			
			String[] hints = KeyMap.getHints(buttonStates);
			
			//MAKE HINTS PWETTY!

			String[] sortedChordHints = new String[4];
			String[] keyHints = new String[4];

			for (int i = 0; i < hints.length; i++) {
				String[] bothHints = hints[i].split(" ");
				for (int j = 0; j < bothHints.length; j++) {
					char[] arr = bothHints[j].toCharArray();
					Arrays.sort(arr);
					StringBuilder sort = new StringBuilder();
					for (int k = 0; k < arr.length; k++) {
						sort.append(arr[k]);
						if (j == 0) { //format first string
							sort.append("\n");
						}
					}
					String sorted = sort.toString();
					bothHints[j] = sorted;
				}
				hints[i] = "";
				for (int k = 0; k< bothHints.length; k++) {
					hints[i] += bothHints[k];
				}
				
				//hints[i] = keyHints[i] + sortedChordHints[i];
			}
			
            /*
			int[] pointers = new int[4];
			String[] newChordHints = {"","","",""};

			HashSet<Character> uniqueChords = new HashSet<Character>();
			for (int i = 0; i < sortedChordHints.length;i++) {
				for (int j = 0; j < sortedChordHints[i].length(); j++) {
					uniqueChords.add(sortedChordHints[i].charAt(j));
				}
			}
			int target = uniqueChords.size();

			for (int i = 0; i < target; i++) {
				char[] toCompare = new char[4];
				char[] compareCopy = new char[4];
				for (int j = 0; j < toCompare.length; j++) {
					if (sortedChordHints[j].length() <= pointers[j]) {
						toCompare[j] = 255; //highest possible val, will not be lowest
					    compareCopy[j] = 255;
					}
					else {
						toCompare[j] = sortedChordHints[j].charAt(pointers[j]);
						compareCopy[j] = sortedChordHints[j].charAt(pointers[j]);
					}
				}
				Arrays.sort(toCompare);
				System.out.println("adfsadfsa    " + new String(toCompare));
				char lowest = toCompare[0];
				for (int j = 0; j < toCompare.length; j++) {
					if ((int) lowest == (int) compareCopy[j]) {
						newChordHints[j] += Character.toString(lowest);
						System.out.println(j + " " + newChordHints[j] + pointers[j]);
						pointers[j] += 1;
					}
					else if ((int) lowest < (int) compareCopy[j]) {
						newChordHints[j] += " ";
					}
				}
			}
			
			for (int i = 0; i < hints.length; i++) {
				hints[i] = keyHints[i] + newChordHints[i];
			}*/

			for (int i = 0; i < buttonStates.length; i++) {
				TextView view = (TextView) findViewById(buttonID[i]);
				view.setText(hints[i]);
				
				if (buttonStates[i] == 0) {
					view.setBackgroundColor(Color.rgb(255, 204, 51));
				} else if (buttonStates[i] == 1) {
					view.setBackgroundColor(Color.rgb(255, 102, 51));
				} else if (buttonStates[i] == 2) {
					view.setBackgroundColor(Color.rgb(255, 51, 102));
				} else if (buttonStates[i] == 3) {
					view.setBackgroundColor(Color.rgb(255, 51, 204));
				} else {
					// TODO: handle invalid button state
				}
			}
		}

		private int findButton(float x, float y) { // returns button 0,1,2,or 3
			Rect range = new Rect();
			int result = 0;
			for (int i = 0; i < buttonID.length; i++) {
				TextView view = (TextView) findViewById(buttonID[i]);
				if (isPointInsideView(x, y, view)) {
					result = i;
					break;
				}
			}
			return result;
		}

		private boolean isPointInsideView(float x, float y, View view) {
			int location[] = new int[2];
			view.getLocationOnScreen(location);
			int viewX = location[0];
			int viewY = location[1];

			// point is inside view bounds
			if ((x > viewX && x < (viewX + view.getWidth()))
					&& (y > viewY && y < (viewY + view.getHeight()))) {
				return true;
			} else {
				return false;
			}
		}

	}
}

