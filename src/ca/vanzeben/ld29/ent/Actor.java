package ca.vanzeben.ld29.ent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.newdawn.gdx.path.Path;
import org.newdawn.gdx.path.PathMover;

import ca.vanzeben.ld29.env.Level;
import ca.vanzeben.ld29.states.InGameState;

public class Actor implements PathMover {
  
  private InGameState        _state;
  
  private static final int   NORTH                 = 3;
  private static final int   WEST                  = 1;
  private static final int   SOUTH                 = 2;
  private static final int   EAST                  = 0;
  private static final int[] FRAMES                = { 0, 1, 2 };
  private int                _type;
  private int                _dir;
  private String             _name;
  private float              _x;
  private float              _y;
  private Level              _level;
  private float              _dx;
  private float              _dy;
  private int                _tx;
  private int                _ty;
  private boolean            _dead;
  private boolean            _active;
  private int                _moveToX              = -1;
  private int                _moveToY              = -1;
  private int                _frameUpdateCounter   = 0;
  private int                _movementCounter      = 0;
  private int                _stepCount            = 0;
  private Runnable           _cleanup;
  private int                _pause;
  private boolean            _dirty;
  private float              _speed                = 0.1F;
  private Path               _currentPath;
  private int                _currentStep;
  private int                _frame                = 0;
  private int                _walkingRange         = 0;
  private int                _targetRange          = 0;
  private Random             random                = new Random();
  private boolean            _playerControlled     = false;
  private int[]              _allowedCapturedTeams = new int[] {};
  private Actor              _targetActor;
  private ArrayList<Actor>   _targetingActors      = new ArrayList<Actor>();
  private boolean            _currentlyTargeting   = false;
  private boolean            _ally                 = false;
  private float              _captureRange         = 0.0F;
  private int                _team                 = 0;
  public int                 _tileBaseIndex        = 0;
  private int                _health               = 3;
  private boolean            _knownVirus           = false;
  private boolean            _isAttackingPlayer    = false;
  
  public Actor(InGameState state, String name, int type, int dir, int team, int x, int y) {
    _state = state;
    _name = name;
    _type = type;
    _dir = dir;
    _team = team;
    _x = x;
    _y = y;
    _tx = x;
    _ty = y;
    _moveToX = getX();
    _moveToY = getY();
    if (team == 0) {
      _playerControlled = true;
      _captureRange = 1.5F;
    }
    if (team == 2) {
      _allowedCapturedTeams = new int[] { 0 };
    }
    if (team == 3) {
      _allowedCapturedTeams = new int[] { 1 };
    }
    if (team == 1) {
      _ally = true;
      _captureRange = 1F;
    }
  }
  
  public ArrayList<Actor> getTargetingActors() {
    return _targetingActors;
  }
  
  @Override
  public int getHeight() {
    return 1;
  }
  
  @Override
  public int getWidth() {
    return 1;
  }
  
  @Override
  public int getX() {
    return (int) _x;
  }
  
  @Override
  public int getY() {
    return (int) _y;
  }
  
  public float getFX() {
    return _x;
  }
  
  public float getFY() {
    return _y;
  }
  
  public float getCenterX() {
    return _x;
  }
  
  public float getCenterY() {
    return _y;
  }
  
  private boolean _eating = false;
  
  public void eat(Actor whoEating) {
    if (!_eating) {
      _eating = true;
      _frame = 0;
      _type += 1;
      whoEating._health -= 1;
    }
  }
  
  public void digest() {
    if (_eating) {
      _eating = false;
      _frame = -1;
      _type -= 1;
    }
  }
  
  public int getFrameTileId() {
    return getDir() * 3 + getFrame() + getType() * 21;
  }
  
  public int getHealth() {
    return _health;
  }
  
  public void damage() {
    _health--;
  }
  
  public boolean update() {
    _frameUpdateCounter++;
    if (_frameUpdateCounter >= 10) {
      _frameUpdateCounter = 0;
      if (_eating && _frame + 1 >= FRAMES.length) {
        digest();
      } else {
        _frame = (_frame + 1) % FRAMES.length;
      }
    }
    if (isMoving()) {
      _stepCount -= 1;
      if (_dx > 0.0F) {
        _x += _speed;
        _dir = EAST;
      }
      if (_dx < 0.0F) {
        _x -= _speed;
        _dir = WEST;
      }
      if (_dy > 0.0F) {
        _y += _speed;
        _dir = SOUTH;
      }
      if (_dy < 0.0F) {
        _y -= _speed;
        _dir = NORTH;
      }
      if (_stepCount == 0) {
        _x = _tx;
        _y = _ty;
        _level.actorMovedToCoord(this, getX(), getY());
        if (_cleanup != null) {
          _cleanup.run();
          _cleanup = null;
        }
      }
    } else {
      executeAI();
    }
    
    if (_targetingActors.size() > 0) {
      for (int i = 0; i < _targetingActors.size(); i++) {
        Actor _targeter = _targetingActors.get(i);
        _targeter.moveToTarget(_moveToX, _moveToY);
      }
    }
    return isMoving();
  }
  
  private void executeAI() {
    if (_playerControlled) { return; }
    if (_name.equalsIgnoreCase("base_enemy")) {
      _walkingRange = 10;
      _targetRange = 5;
      
    } else if (_name.equalsIgnoreCase("dummy")) {
      if (_speed > 0.075F) {
        _speed = 0.085F;
      }
      _walkingRange = 25;
      
    }
    if (_targetActor == null && _targetRange > 0) {
      if (_level.getLeader().isKnownVirus() && _level.hasLOS(getCenterX(), getCenterY(), _level.getLeader().getCenterX(), _level.getLeader().getCenterY())) {
        setTargetActor(_level.getLeader());
      } else {
        _targetActor = null;
      }
    }
    if (_walkingRange > 0) {
      _movementCounter++;
      if (_movementCounter >= random.nextInt(50) + 75) {
        _movementCounter = 0;
        int toX = getX();
        int toY = getY();
        for (int i = 0; i < 10; i++) {
          toX = random.nextInt(_walkingRange) - _walkingRange / 2 + getX();
          toY = random.nextInt(_walkingRange) - _walkingRange / 2 + getY();
          if (_level.hasFloor(toX, toY)) {
            break;
          }
        }
        moveToTarget(toX, toY);
      }
    }
  }
  
  public boolean isKnownVirus() {
    return _knownVirus;
  }
  
  public Actor getClosestEnemy() {
    Actor target = null;
    List<Actor> actors = _level.getActors();
    for (int i = 0; i < actors.size(); i++) {
      Actor a = actors.get(i);
      if (a.getTeam() != 3) {
        continue;
      }
      if (a.getX() >= getX() && a.getX() <= getX() && a.getY() >= getY() && a.getY() <= getY()) {
        if (target == null || (target.getX() >= a.getX() && target.getX() <= a.getX() && target.getY() >= a.getY() && target.getY() <= a.getY())) {
          target = a;
        }
      }
    }
    return target;
  }
  
  public boolean isLeader() {
    return _name.equals("leader");
  }
  
  public int getFrame() {
    if (_frame < 0) {
      _frame = 0;
    }
    return FRAMES[_frame];
  }
  
  public boolean isMoving() {
    return _stepCount > 0;
  }
  
  public boolean moveTo(int tx, int ty) {
    _pause -= 1;
    if (isMoving() || _pause > 0) {
      _stepCount = 0;
      _dx = 0.0F;
      _dy = 0.0F;
      return false;
    }
    
    if (!isLeader() && _ally) {
      Actor target = _level.getActorAt(tx, ty, this);
      if (target != null) {
        _stepCount = 0;
        _dx = 0;
        _dy = 0;
        return false;
      }
    }
    
    _stepCount = 0;
    _dx = 0.0F;
    _dy = 0.0F;
    
    move(tx, ty);
    boolean moving = isMoving();
    return moving;
  }
  
  public boolean canTeamCapture(int teamId) {
    if ((_team == 3 && !_level.getLeader().isKnownVirus())) { return false; }
    for (int i : _allowedCapturedTeams) {
      if (teamId == i) { return true; }
    }
    return false;
  }
  
  public void move(int tx, int ty) {
    Actor target = _level.getActorAt(tx, ty, this);
    
    _tx = tx;
    _ty = ty;
    
    if (target != null && target != this) {
      if (getTeam() == 1 && target.getTeam() == 1) {
        if (((target._tx == getX()) && (target._ty == getY())) || ((target._tx == target.getX()) && (target._ty == target.getY()))) {
          target._tx = getX();
          target._ty = getY();
          target._dx = (target._tx - target.getX());
          target._dy = (target._ty - target.getY());
          
          target._stepCount = 10;
          target._dirty = true;
        } else {
          _pause = (1 + (int) (Math.random() * 3.0D));
        }
      } else if (getTeam() == 3 && target.getTeam() == 3) {
        if (((target._tx == getX()) && (target._ty == getY())) || ((target._tx == target.getX()) && (target._ty == target.getY()))) {
          target._tx = getX();
          target._ty = getY();
          target._dx = (target._tx - target.getX());
          target._dy = (target._ty - target.getY());
          
          target._stepCount = 10;
          target._dirty = true;
        } else {
          _pause = (1 + (int) (Math.random() * 3.0D));
        }
        return;
      } else if (getTeam() == 3 && target.isLeader() && target._knownVirus) {
        if (((target._tx == getX()) && (target._ty == getY())) || ((target._tx == target.getX()) && (target._ty == target.getY()))) {
          eat(target);
        }
      }
    }
    _dx = (tx - getX());
    _dy = (ty - getY());
    _stepCount = 10;
    _dirty = true;
  }
  
  public int getTeam() {
    return _team;
  }
  
  public boolean inRangeOf(Actor actor) {
    if (actor == null) { return false; }
    return (getFX() - actor._captureRange / 2 <= actor.getFX() && actor.getFX() <= getFX() + actor._captureRange / 2 && getFY() - actor._captureRange / 2 <= actor.getFY() && actor.getFY() <= getFY()
        + actor._captureRange / 2);
  }
  
  public void capture(Actor capturer) {
    if (capturer.getTeam() == 0) {
      if (capturer.hasTeamNear(3)) {
        capturer._knownVirus = true;
      }
      _dead = true;
      Actor ally = new Actor(_state, "ally", 1, getDir(), 1, getX(), getY());
      ally._dx = _dx;
      ally._dy = _dy;
      ally._x = _x;
      ally._y = _y;
      _level.addActor(ally);
    } else {
      capturer._dead = true;
      _dead = true;
    }
  }
  
  public boolean hasTeamNear(int teamId) {
    ArrayList<Actor> actors = _level.getActors();
    for (int i = 0; i < actors.size(); i++) {
      Actor a = actors.get(i);
      if (a == this || a._team != teamId) {
        continue;
      }
      if (_level.hasLOS(getCenterX(), getCenterY(), a.getCenterX(), a.getCenterY())) { return true; }
    }
    return false;
  }
  
  public boolean isAt(int x, int y) {
    if (isMoving()) {
      if (_tx == x && _ty == y) { return true; }
      return false;
    }
    return x == getX() && y == getY();
  }
  
  public void setPath(Path path) {
    _currentPath = path;
    _currentStep = 1;
  }
  
  private boolean nextPathStep() {
    if (_currentPath != null) {
      if (_currentStep >= _currentPath.getLength()) {
        _currentPath = null;
        return false;
      }
      int nx = _currentPath.getX(_currentStep);
      int ny = _currentPath.getY(_currentStep);
      if (moveTo(nx, ny)) {
        _currentStep++;
        return true;
      }
    }
    return false;
  }
  
  public boolean step(InGameState state) {
    if (getTeam() == 1) {
      Actor leader = _level.getLeader();
      if (leader != this) {
        Actor closest = getClosestEnemy();
        if (closest != null && _level.hasLOS(getCenterX(), getCenterY(), closest.getCenterX(), closest.getCenterY())) {
          _moveToX = closest.getX();
          _moveToY = closest.getY();
          if (closest != null) {
            setPath(_level.findPath(closest.getX(), closest.getY(), this));
          }
        } else {
          _moveToX = getX();
          _moveToY = getY();
          if (leader != null) {
            setPath(_level.findPath(leader.getX(), leader.getY(), this));
          }
        }
      }
    }
    boolean moved = nextPathStep();
    if (!moved && (_level.getLeader() == this || getTeam() != 1) && ((getX() != _moveToX) || (getY() != _moveToY))) {
      setPath(_level.findPath(_moveToX, _moveToY, this));
      nextPathStep();
    }
    return moved;
  }
  
  public void moveToTarget(int xp, int yp) {
    if (_currentlyTargeting) {
      _currentlyTargeting = false;
      _targetActor._targetingActors.remove(this);
      _targetActor = null;
    }
    _moveToX = xp;
    _moveToY = yp;
    setPath(_level.findPath(_moveToX, _moveToY, this));
  }
  
  public void moveToTarget(Actor actor) {
    moveToTarget(actor.getX(), actor.getY());
  }
  
  public void setLevel(Level level) {
    _level = level;
  }
  
  public int getDir() {
    return _dir;
  }
  
  public int getType() {
    return _type;
  }
  
  public String toString() {
    return "[" + _name + "] @ (" + getX() + ", " + getY() + ")";
  }
  
  public int getStepCount() {
    return _stepCount;
  }
  
  public boolean isDirty() {
    return _dirty;
  }
  
  public boolean isDead() {
    return _dead;
  }
  
  public void setTargetActor(Actor actor) {
    _currentlyTargeting = true;
    _targetActor = actor;
    actor._targetingActors.add(this);
    moveToTarget(actor);
  }
  
  public void activate() {
    _active = true;
  }
  
}
