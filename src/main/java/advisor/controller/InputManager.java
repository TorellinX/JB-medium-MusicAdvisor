package advisor.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputManager {

  static String getInput() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String line;
    try {
      line = reader.readLine().strip();
      return line;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}

