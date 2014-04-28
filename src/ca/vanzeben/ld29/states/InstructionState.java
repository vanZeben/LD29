package ca.vanzeben.ld29.states;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.gdx.Button;
import org.newdawn.gdx.State;
import org.newdawn.gdx.StateBasedGame;

import ca.vanzeben.ld29.Control;
import ca.vanzeben.ld29.Resources;
import ca.vanzeben.ld29.VirusGame;

public class InstructionState implements State {
  private static StateBasedGame _game;
  private static List<Control>  _controls = new ArrayList<Control>();
  
  @Override
  public void controlPressed(int arg0, Button arg1) {
  }
  
  @Override
  public void controlReleased(int arg0, Button arg1) {
  }
  
  private int _frameUpdateCounter;
  private int _currentFrame = 0;
  
  @Override
  public void update() {
    _frameUpdateCounter++;
    if (_frameUpdateCounter >= 10) {
      _frameUpdateCounter = 0;
      _currentFrame = (_currentFrame + 1) % 3;
      _brainFrame = (_brainFrame + 1) % 8;
    }
  }
  
  private String info        = "You play as Vinnie, the virus who has gone *beneath the surface* of the skin to invade the blood stream. To move simply click where you want to move to and vinnie will do the rest. Stay away from the White blood cells as they will attempt to hunt and destroy you. Capture red blood cells to turn them into your allies. Allies will take hits from a white blood cell for you, if one get's too close. Take your infected army to attack the brain hidden in the arteries! ";
  private int    _brainFrame = 0;
  
  @Override
  public void draw() {
    int pixelScale = 64;
    int textScale = 3;
    for (int x = 0; x < (_game.getWidth() / pixelScale) + 1; x++) {
      for (int y = 0; y < (_game.getHeight() / pixelScale) + 1; y++) {
        Resources.ENV.draw(x * pixelScale, y * pixelScale, pixelScale, pixelScale, 32);
      }
    }
    Resources.GUI_INSTRUCTION.draw(10, 10, (_game.getWidth() - 20), (_game.getHeight() - 20 - 60 * textScale));
    int unitHeights = 56;
    int textHeights = 32;
    int baseX = 270;
    int paddingX = 130;
    Resources.LOGO.draw(5, (_game.getHeight() - 50 * textScale), 256 * textScale, 256 * textScale);
    Resources.UNITS.draw(baseX + paddingX * 0, _game.getHeight() - unitHeights * textScale, pixelScale, pixelScale, getFrameId(0));
    Resources.MINI.draw("Vinnie", baseX + paddingX * 0 - (Resources.MINI.getWidth("Vinnie") / 4) + 20, _game.getHeight() - textHeights * textScale);
    
    Resources.ENV.draw(baseX + paddingX * 2 - 50, _game.getHeight() - unitHeights * textScale, pixelScale, pixelScale, _brainFrame + 3 * 16);
    Resources.MINI.draw("Objective", baseX + paddingX * 2 - 50 - 10, _game.getHeight() - textHeights * textScale);
    
    Resources.UNITS.draw(baseX + paddingX * 1 / 2, _game.getHeight() - unitHeights * textScale, pixelScale, pixelScale, getFrameId(1));
    Resources.MINI.draw("Allied", baseX + paddingX * 1 / 2 - (Resources.MINI.getWidth("Allied") / 4) + 20, _game.getHeight() - textHeights * textScale);
    Resources.UNITS.draw(baseX + paddingX * 3 - 20, _game.getHeight() - unitHeights * textScale, pixelScale, pixelScale, getFrameId(2));
    Resources.MINI.drawWrapped("White Blood", baseX + paddingX * 3 - 20 - (Resources.MINI.getWidth("White Blood") / 4), _game.getHeight() - textHeights * textScale, 125);
    Resources.UNITS.draw(baseX + paddingX * 4 - 20, _game.getHeight() - unitHeights * textScale, pixelScale, pixelScale, getFrameId(4));
    Resources.MINI.drawWrapped("Red Blood", baseX + paddingX * 4 - 20 - (Resources.MINI.getWidth("Red Blood") / 4), _game.getHeight() - textHeights * textScale, 125);
    
    Resources.FONT.drawWrapped(info, 30, 30, (_game.getWidth() - 60));
    for (int i = 0; i < _controls.size(); i++) {
      Control control = _controls.get(i);
      if (i == 0) {
        control.draw(_game, 0);
      }
    }
  }
  
  private int getFrameId(int type) {
    return 2 * 3 + _currentFrame + type * 21;
  }
  
  @Override
  public void enter() {
  }
  
  @Override
  public void keyPressed(int arg0) {
  }
  
  @Override
  public void keyReleased(int arg0) {
  }
  
  @Override
  public void leave() {
  }
  
  @Override
  public void mouseAltPressed(int arg0, int arg1) {
  }
  
  @Override
  public void mouseDown(int x, int y) {
    for (int i = 0; i < _controls.size(); i++) {
      Control control = _controls.get(i);
      if (control.contains(_game, x, y)) {
        control.setPressed(true);
      }
    }
  }
  
  @Override
  public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
  }
  
  @Override
  public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
  }
  
  @Override
  public void mouseUp(int x, int y) {
    for (int i = 0; i < _controls.size(); i++) {
      Control control = _controls.get(i);
      if (control.isPressed()) {
        control.setPressed(false);
        if (control.equals(_controls.get(0))) {
          _game.enterState(VirusGame.STATE_TITLE);
        }
      }
    }
  }
  
  @Override
  public void preEnter() {
  }
  
  @Override
  public void setup(StateBasedGame game, int screenWidth, int screenHeight) {
    _game = game;
    _controls.add(new Control(_game.getWidth() - 250 - 25, _game.getHeight() - 32 - 25, 250, 32, "Back!"));
  }
}