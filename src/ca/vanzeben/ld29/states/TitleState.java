package ca.vanzeben.ld29.states;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.gdx.Button;
import org.newdawn.gdx.State;
import org.newdawn.gdx.StateBasedGame;

import ca.vanzeben.ld29.Control;
import ca.vanzeben.ld29.VirusGame;
import ca.vanzeben.ld29.Resources;

public class TitleState implements State {
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
    }
  }
  
  @Override
  public void draw() {
    int pixelScale = 64;
    int textScale = 5;
    for (int x = 0; x < (_game.getWidth() / pixelScale) + 1; x++) {
      for (int y = 0; y < (_game.getHeight() / pixelScale) + 1; y++) {
        Resources.ENV.draw(x * pixelScale, y * pixelScale, pixelScale, pixelScale, 32);
      }
    }
    
    Resources.LOGO.draw(5, (_game.getHeight() - 50 * textScale), 256 * textScale, 256 * textScale);
    Resources.UNITS.draw(_game.getWidth() - textScale * pixelScale - 50, _game.getHeight() / 2 - textScale * pixelScale / 2 - 100, textScale * pixelScale, textScale * pixelScale,
        2 * 3 + _currentFrame);
    
    for (int i = 0; i < _controls.size(); i++) {
      Control control = _controls.get(i);
      int color = 1;
      if (i == 0) {
        color = 1;
      } else if (i == 1) {
        color = 2;
      } else {
        color = 0;
      }
      control.draw(_game, color);
    }
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
  
  private int _diff = 2;
  
  @Override
  public void leave() {
    switch (_diff) {
      default:
      case 2:
        VirusGame.STATE_GAME.setDefaults(128, 128, 10, 15, 0);
        break;
      case 3:
        VirusGame.STATE_GAME.setDefaults(128, 128, 25, 35, 1);
        break;
      case 4:
        VirusGame.STATE_GAME.setDefaults(128, 128, 45, 75, 2);
        break;
    }
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
        if (i >= 2) {
          control.setToggled(true);
        }
      }
    }
  }
  
  @Override
  public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
  }
  
  @Override
  public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
  }
  
  private void resetDiff() {
    _controls.get(2).setToggled(true);
    _controls.get(3).setToggled(false);
    _controls.get(4).setToggled(false);
  }
  
  @Override
  public void mouseUp(int x, int y) {
    int pressedControl = -1;
    for (int i = 0; i < _controls.size(); i++) {
      Control control = _controls.get(i);
      if (control.isPressed()) {
        pressedControl = i;
        _controls.get(i).setPressed(false);
      }
      if (pressedControl == 0) {
        _game.enterState(VirusGame.STATE_GAME);
        _controls.get(pressedControl).setPressed(false);
      } else if (pressedControl == 1) {
        _game.enterState(VirusGame.STATE_HELP);
      } else if (pressedControl == 2 || pressedControl == 3 || pressedControl == 4) {
        _diff = pressedControl;
        for (int ii = 2; ii < 5; ii++) {
          if (ii == pressedControl) {
            _controls.get(ii).setToggled(true);
            continue;
          }
          _controls.get(ii).setToggled(false);
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
    _controls.add(new Control(32, 32 * 0 + 10 * 1, _game.getWidth() / 3, _game.getHeight() / 15, "Start Game!"));
    _controls.add(new Control(32, 32 * 1 + 10 * 2, _game.getWidth() / 3, _game.getHeight() / 15, "Instructions!"));
    
    _controls.add(new Control(32, 10 + 32 * 2 + 10 * 3, _game.getWidth() / 3, _game.getHeight() / 15, "Easy"));
    _controls.add(new Control(32, 10 + 32 * 3 + 10 * 4, _game.getWidth() / 3, _game.getHeight() / 15, "Normal"));
    _controls.add(new Control(32, 10 + 32 * 4 + 10 * 5, _game.getWidth() / 3, _game.getHeight() / 15, "Hard"));
    resetDiff();
  }
}