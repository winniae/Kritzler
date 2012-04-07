package com.tinkerlog.kritzler;


import java.util.List;

import processing.core.PApplet;
import processing.serial.Serial;

/**
 * This class talks to the Kritzler Arduino firmware.
 * Instantiate an object with a given port to talk to an Kritzler.
 * Use Instruction objects and
 * a) set a list via setInstructions() and call checkSerial() repeatedly or
 * b) call sendInstruction() directly.
 */
public class Kritzler {
    
  private Serial port;
  private List<Instruction> instructions;
  private int currentInst;
  private StringBuilder buf = new StringBuilder();

  // offset and scale factor
  private float tx, ty;
  private float scale;
  
  public Kritzler(Serial port) {
    this.port = port;
  }
  
  public void setInstructions(List<Instruction> instructions) {
    this.instructions = instructions;
    this.currentInst = 0;
  }
  
  public void translate(float x, float y) {
    this.tx = x;
    this.ty = y;
  }
  
  public void setScale(float s) {
    this.scale = s;
  }

  public boolean isFinished() {
    if (port == null) return true;
    return instructions.size() == currentInst;
  }

  /**
   * Tests serial connection.
   * Receives messages from Arduino.
   * Sends the next instruction in the list.
   */
  public void checkSerial() {
    if (port != null && port.available() > 0) {
      processSerial();
    }
  }

  /**
   * Receive message from Arduino and process message
   */
  private void processSerial() {
    while (port.available() > 0) {
      int c = port.read();
      if (c != 10) { // TODO magic number? what is it?
        buf.append((char)c);        
      }
      else {
        String message = buf.toString();
        buf.setLength(0);
        if (message.length() > 0) {
          message = message.substring(0, message.length()-1);
        }
        if (message.startsWith("#")) {
          System.out.println("bot: " + message);
        }
        else {          
          processMessage(message);
        }        
      }
    }
  }

  /**
   * Process message received from Arduino.
   * Calls next instruction in the list.
   * @param message
   */
  private void processMessage(String message) {
    if (message.equals("OK")) {
      System.out.println("received ok");
      if (instructions != null) {
        if (currentInst >= instructions.size()) {
          System.out.println("nothing to do");
        }
        else {
          Instruction inst = instructions.get(currentInst);
          currentInst++;
          sendInstruction(inst);
        }
      }      
    }
    else {
      System.out.println("unknown: " + message);
    }
  }

  /**
   * Directly send an instruction to be performed
   * @param i
   */
  public void sendInstruction(Instruction i) {
    if (port == null) return;
    String msg = null;
    int x = (int)(i.x * scale);
    int y = (int)(i.y * scale);    
    switch (i.type) {
    case Instruction.MOVE_ABS:
      msg = "M " + (int)(x + tx) + " " + (int)(y + ty) + '\r';
      break;
    case Instruction.LINE_ABS:
      msg = "L " + (int)(x + tx) + " " + (int)(y + ty) + '\r';
      break;
    }
    System.out.println("sending (" + currentInst + "): " + msg);
    port.write(msg);
  }
  
}
