package ca.vanzeben.ld29;

import org.newdawn.gdx.StateBasedGame;

import ca.vanzeben.ld29.states.InGameState;
import ca.vanzeben.ld29.states.InstructionState;
import ca.vanzeben.ld29.states.LoseGameState;
import ca.vanzeben.ld29.states.TitleState;
import ca.vanzeben.ld29.states.WinGameState;

public class VirusGame extends StateBasedGame {
  
  public static final TitleState       STATE_TITLE = new TitleState();
  public static final InGameState      STATE_GAME  = new InGameState();
  public static final LoseGameState    STATE_LOSS  = new LoseGameState();
  public static final WinGameState     STATE_WIN   = new WinGameState();
  public static final InstructionState STATE_HELP  = new InstructionState();
  
  public VirusGame(int width, int height) {
    super(width, height);
  }
  
  public VirusGame() {
  }
  
  public void create() {
    super.create();
    
    Resources.init();
    
    addState(STATE_TITLE);
    addState(STATE_GAME);
    addState(STATE_LOSS);
    addState(STATE_HELP);
    addState(STATE_WIN);
    enterState(STATE_TITLE);
  }
}