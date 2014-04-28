package ca.vanzeben.ld29;

import org.newdawn.gdx.Font;
import org.newdawn.gdx.Image;
import org.newdawn.gdx.SimpleTileSet;

import ca.vanzeben.ld29.mapgen.RoomTemplates;

public class Resources {
  public static SimpleTileSet ENV;
  public static SimpleTileSet UNITS;
  public static Image         LOGO;
  public static Font          FONT;
  public static Font          MINI;
  public static RoomTemplates TEMPLATES;
  public static SimpleTileSet COLOURS;
  public static Image         GUI_INSTRUCTION;
  
  public static void init() {
    ENV = new SimpleTileSet("data/env.png", 16, 16);
    UNITS = new SimpleTileSet("data/units.png", 12, 12);
    COLOURS = new SimpleTileSet("data/colours.png", 4, 4);
    LOGO = new Image("data/logo.png");
    GUI_INSTRUCTION = new Image("data/gui/instruction.png");
    
    FONT = new Font("data/fonts/visitor.fnt");
    MINI = new Font("data/fonts/mini.fnt");
    TEMPLATES = new RoomTemplates();
  }
}
