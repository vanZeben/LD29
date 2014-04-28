package ca.vanzeben.ld29;

import org.newdawn.gdx.SimpleTileSet;
import org.newdawn.gdx.StateBasedGame;

public class Control {
  private static final int PADDING = 3;
  private int              _x;
  private int              _y;
  private int              _width;
  private int              _height;
  private int              _index;
  private SimpleTileSet    _set;
  private String           _label;
  private boolean          _pressed;
  private boolean          _toggled;
  
  public Control(int x, int y, int width, int height, SimpleTileSet set, int index) {
    _x = x;
    _y = x;
    _width = width;
    _height = height;
    _set = set;
    _index = index;
    _pressed = false;
    _toggled = false;
  }
  
  public Control(int x, int y, int width, int height, String label) {
    _x = x;
    _y = y;
    _width = width;
    _height = height;
    _label = label;
    _pressed = false;
    _toggled = false;
  }
  
  public boolean isToggled() {
    return _toggled;
  }
  
  public void setToggled(boolean toggled) {
    _toggled = toggled;
  }
  
  public boolean isPressed() {
    return _pressed;
  }
  
  public void setPressed(boolean pressed) {
    _pressed = pressed;
  }
  
  public void draw(StateBasedGame game, int col) {
    Resources.COLOURS.draw(_x, _y, _width, _height, 4 * (col / 4 + 1) + col);
    
    if (!_pressed && _toggled) {
      Resources.COLOURS.draw(_x + PADDING, _y + PADDING, _width - PADDING * 2, _height - PADDING * 2, 4 * (col / 4) + col);
    }
    if (_label != null) {
      Resources.FONT.drawCentered(_label, _x, _y + 7, _width);
    }
    
    if (_set != null) {
      _set.draw(_x + 3, _y + 3, 48.0F, 48.0F, _index);
    }
  }
  
  public boolean contains(StateBasedGame game, int x2, int y2) {
    return x2 >= _x && y2 >= _y && x2 < _x + _width && y2 < _y + _height;
  }
  
  public String getLabel() {
    return _label;
  }
}
