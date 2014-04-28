package ca.vanzeben.ld29.ent.objectives;

import ca.vanzeben.ld29.ent.Objective;
import ca.vanzeben.ld29.ent.TileEntity;
import ca.vanzeben.ld29.env.Level;

public class BrainObjective implements TileEntity, Objective {
  
  private Level              _level;
  private int                _x;
  private int                _y;
  private static final int[] FRAMES          = { 0, 1, 2, 3, 4, 5, 6, 7 };
  private int                _updateInterval = 0;
  private int                _currentFrame   = 0;
  
  public BrainObjective(Level level, int x, int y) {
    _level = level;
    _x = x;
    _y = y;
  }
  
  public void setX(int x) {
    _x = x;
  }
  
  public void setY(int y) {
    _y = y;
  }
  
  @Override
  public int getX() {
    return _x;
  }
  
  @Override
  public int getY() {
    return _y;
  }
  
  @Override
  public int getCurrentFrameId() {
    return getFrame() + 3 * 16;
  }
  
  private int getFrame() {
    if (_currentFrame < 0) {
      _currentFrame = 0;
    }
    return _currentFrame;
  }
  
  @Override
  public void update() {
    _updateInterval++;
    if (_updateInterval >= 10) {
      _updateInterval = 0;
      _currentFrame = (_currentFrame + 1) % FRAMES.length;
    }
    if (_level.hasLOS(getX(), getY(), _level.getLeader().getCenterX(), _level.getLeader().getCenterY())
        && (_level.getLeader().getCenterX() >= getX() - 2 && _level.getLeader().getCenterX() <= getX() + 2)
        && (_level.getLeader().getCenterY() >= getY() - 2 && _level.getLeader().getCenterY() <= getY() + 2)) {
      _level.attemptWin();
    }
  }
  
  @Override
  public boolean complete() {
    return false;
  }
  
  @Override
  public boolean requiresDiscovery() {
    return false;
  }
}
