package ca.vanzeben.ld29.states;

import org.newdawn.gdx.Button;
import org.newdawn.gdx.State;
import org.newdawn.gdx.StateBasedGame;

import ca.vanzeben.ld29.VirusGame;
import ca.vanzeben.ld29.Resources;

public class WinGameState implements State {
  private static StateBasedGame _game;
  
  @Override
  public void controlPressed(int arg0, Button arg1) {
  }
  
  @Override
  public void controlReleased(int arg0, Button arg1) {
  }
  
  @Override
  public void draw() {
    int pixelScale = 64;
    for (int x = 0; x < (_game.getWidth() / pixelScale) + 1; x++) {
      for (int y = 0; y < (_game.getHeight() / pixelScale) + 1; y++) {
        Resources.ENV.draw(x * pixelScale, y * pixelScale, pixelScale, pixelScale, 32);
      }
    }
    
    Resources.UNITS.draw((_game.getWidth() - 130) / 2 - (3 * pixelScale - 125) / 2, (_game.getHeight() - 130) / 2 - (3 * pixelScale ) / 2, 3 * pixelScale, 3 * pixelScale, 2 * 3 + _currentFrame);
    Resources.FONT.draw("You infected the stupid human! ", _game.getWidth() / 2 - Resources.FONT.getWidth("You infected the stupid human! ") / 2, _game.getHeight() / 2 + 50);
    Resources.FONT.draw("Score: " + _score, _game.getWidth() / 2 - Resources.FONT.getWidth("Score: " + _score) / 2, _game.getHeight() / 2 + 90);
  }
  
  @Override
  public void enter() {
  }
  
  @Override
  public void keyPressed(int arg0) {
    _game.enterState(VirusGame.STATE_TITLE);
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
  public void mouseDown(int arg0, int arg1) {
    _game.enterState(VirusGame.STATE_TITLE);
  }
  
  @Override
  public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
  }
  
  private int _score = 0;
  
  public void setScore(double percent) {
    _score = (int) (percent * 13);
  }
  
  @Override
  public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
  }
  
  @Override
  public void mouseUp(int arg0, int arg1) {
  }
  
  @Override
  public void preEnter() {
  }
  
  @Override
  public void setup(StateBasedGame game, int screenWidth, int screenHeight) {
    _game = game;
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
  
}