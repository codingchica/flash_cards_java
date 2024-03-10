package com.codingchica.flashcards.component.model;

import java.util.ArrayList;
import java.util.List;

public class CLIWorld {
  public List<String> arguments = new ArrayList<>();
  public int exitCode = 0;
  public List<String> outputLines = new ArrayList<>();
  public List<String> errorOutputLines = new ArrayList<>();
}
