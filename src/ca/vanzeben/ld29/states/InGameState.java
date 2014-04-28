package ca.vanzeben.ld29.states;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.gdx.Button;
import org.newdawn.gdx.State;
import org.newdawn.gdx.StateBasedGame;

import com.badlogic.gdx.Gdx;

import ca.vanzeben.ld29.Control;
import ca.vanzeben.ld29.Resources;
import ca.vanzeben.ld29.VirusGame;
import ca.vanzeben.ld29.ent.Actor;
import ca.vanzeben.ld29.ent.TileEntity;
import ca.vanzeben.ld29.ent.objectives.BrainObjective;
import ca.vanzeben.ld29.env.Level;

public class InGameState implements State {
  private static StateBasedGame _game;
  private Level                 _level;
  private static final int      TILE_SIZE    = 40;
  private float                 _xOffs       = 0;
  private float                 _yOffs       = 0;
  private float                 _targetXOffs = 0;
  private float                 _targetYOffs = 0;
  private Actor                 _selected;
  private boolean               _moving;
  private boolean               _playing;
  private boolean               _running     = false;
  private List<Control>         _controls    = new ArrayList<Control>();
  
  private int                   _width;
  private int                   _height;
  private int                   _numMinRooms;
  private int                   _spawnRates;
  private int                   _numMaxRooms;
  
  public void setDefaults(int width, int height, int numMinRooms, int numMaxRooms, int spawnRates) {
    _width = width;
    _height = height;
    _numMinRooms = numMinRooms;
    _numMaxRooms = numMaxRooms;
    _spawnRates = spawnRates;
  }
  
  @Override
  public void controlPressed(int arg0, Button arg1) {
  }
  
  @Override
  public void controlReleased(int arg0, Button arg1) {
  }
  
  public void win(double percentCaught) {
    _bloodHave = percentCaught;
    _won = true;
    _running = false;
    leave();
  }
  
  private boolean _won                 = false;
  private double  _bloodNeeded         = 0.0D;
  private double  _bloodHave           = 0.0D;
  private boolean _showCurrentPercent  = false;
  private int     _currentUpdateTime   = 0;
  private int     _trackingUpdateCount = 0;
  
  public void needMoreBlood(double percentCaught, double percentNeeded) {
    _bloodHave = percentCaught;
    _bloodNeeded = percentNeeded;
    _showCurrentPercent = true;
  }
  
  @Override
  public void update() {
    _trackingUpdateCount++;
    if (_showCurrentPercent) {
      _currentUpdateTime++;
      if (_currentUpdateTime >= 50) {
        _showCurrentPercent = false;
        _currentUpdateTime = 0;
      }
    }
    if (_running) {
      Actor tracking = null;
      if (_selected != null) {
        tracking = _selected;
      }
      if (tracking != null) {
        _targetXOffs = (_game.getWidth() / 2 / TILE_SIZE - tracking.getFX());
        _targetYOffs = (_game.getHeight() / 2 / TILE_SIZE - tracking.getFY());
      }
      _moving = _level.update();
      if (!_moving && _playing) {
        takeTurn();
      }
      int ox = (int) (_xOffs * TILE_SIZE / 4.0F);
      int oy = (int) (_yOffs * TILE_SIZE / 4.0F);
      
      cleanupActors();
      
      List<TileEntity> tileEntities = _level.getTileEntities();
      for (int i = 0; i < tileEntities.size(); i++) {
        tileEntities.get(i).update();
      }
      
      _xOffs = (_targetXOffs * 0.4F + _xOffs * 0.6F);
      _yOffs = (_targetYOffs * 0.4F + _yOffs * 0.6F);
      if (_selected.getHealth() <= 0) {
        _running = false;
        leave();
      }
    }
  }
  
  public void cleanupActors() {
    ArrayList<Actor> actors = _level.getActors();
    for (int i = 0; i < actors.size(); i++) {
      Actor actor = (Actor) actors.get(i);
      
      List<Actor> team = _level.getTeam();
      for (int j = 0; j < team.size(); j++) {
        Actor targeter = team.get(j);
        boolean inRange = actor.inRangeOf(targeter);
        boolean canCapture = actor.canTeamCapture(targeter.getTeam());
        if (inRange && canCapture) {
          actor.capture(targeter);
        }
      }
      if (actor.isDead()) {
        _level.removeActor(actor);
      }
    }
  }
  
  public void takeTurn() {
    ArrayList<Actor> actors = _level.getActors();
    for (int i = 0; i < actors.size(); i++) {
      Actor actor = (Actor) actors.get(i);
      actor.step(this);
    }
  }
  
  @Override
  public void draw() {
    _level.draw();
    int tilesAcross = (_game.getWidth() / TILE_SIZE) + 5;
    int tilesDown = (_game.getHeight() / TILE_SIZE) + 5;
    int baseX = (int) -_xOffs;
    int baseY = (int) -_yOffs;
    int tileCount = 0;
    for (int yc = -1; yc < tilesDown; yc++) {
      for (int xc = -1; xc < tilesAcross; xc++) {
        int x = baseX + xc;
        int y = baseY + yc;
        if (_level.isDiscovered(x, y)) {
          for (int layer = 0; layer < 10; layer++) {
            int render = _level.getRender(x, y, layer);
            if (render > -1) {
              tileCount++;
              float offset = 0.0F;
              if (layer == 4) {
                offset = -(TILE_SIZE * 0.4F);
              }
              Resources.ENV.draw(_xOffs * TILE_SIZE + x * TILE_SIZE, _yOffs * TILE_SIZE + y * TILE_SIZE, TILE_SIZE, TILE_SIZE, render);
            }
          }
        }
      }
    }
    
    ArrayList<Actor> actors = _level.getActors();
    for (int i = actors.size() - 1; i >= 0; i--) {
      Actor actor = (Actor) actors.get(i);
      float x = actor.getFX();
      float y = actor.getFY();
      
      // if (_level.isDiscovered(actor.getX(), actor.getY())) {
      Resources.UNITS.draw(_xOffs * TILE_SIZE + x * TILE_SIZE, _yOffs * TILE_SIZE + y * TILE_SIZE - TILE_SIZE / 8, TILE_SIZE, TILE_SIZE, actor.getFrameTileId());
      // }
    }
    
    List<TileEntity> tileEntities = _level.getTileEntities();
    for (int i = 0; i < tileEntities.size(); i++) {
      TileEntity e = tileEntities.get(i);
      float scale = 1;
      if (e instanceof BrainObjective) {
        scale = 1.5F;
      }
      Resources.ENV.draw(_xOffs * TILE_SIZE + e.getX() * TILE_SIZE - (TILE_SIZE * scale) / 2, _yOffs * TILE_SIZE + e.getY() * TILE_SIZE - (TILE_SIZE * scale) / 2, TILE_SIZE * scale,
          TILE_SIZE * scale, e.getCurrentFrameId());
      if (_showCurrentPercent) {
        String txt = "You can't infect *ME* with only " + Math.round(_bloodHave) + " % of my blood.";
        Resources.MINI.draw(txt, _xOffs * TILE_SIZE + e.getX() * TILE_SIZE - (TILE_SIZE * scale) / 2 - Resources.MINI.getWidth(txt) / 2, _yOffs * TILE_SIZE + e.getY() * TILE_SIZE
            - (TILE_SIZE * scale) / 2 - 20);
      }
    }
    Resources.FONT.draw("Health: " + _selected.getHealth(), (_game.getWidth() + Resources.MINI.getWidth("Lives: " + _selected.getHealth())) / 12 * 4 + 45, 10.0F);
    
    for (int i = 0; i < _controls.size(); i++) {
      ((Control) _controls.get(i)).draw(_game, 1);
    }
  }
  
  @Override
  public void enter() {
  }
  
  @Override
  public void keyPressed(int keyCode) {
  }
  
  @Override
  public void keyReleased(int keyCode) {
  }
  
  @Override
  public void leave() {
    if (_won) {
      VirusGame.STATE_WIN.setScore(_bloodHave);
      _game.enterState(VirusGame.STATE_WIN);
    }
    if (_selected.getHealth() <= 0) {
      _game.enterState(VirusGame.STATE_LOSS);
    }
    
  }
  
  @Override
  public void mouseAltPressed(int x, int y) {
  }
  
  @Override
  public void mouseDown(int x, int y) {
    int xp = (int) (x - _xOffs * TILE_SIZE);
    int yp = (int) (y - _yOffs * TILE_SIZE);
    xp /= TILE_SIZE;
    yp /= TILE_SIZE;
    if (_selected != null) {
      Actor target = _level.getActorAt(xp, yp, _selected);
      if (target != null) {
        _selected.setTargetActor(target);
      } else {
        _selected.moveToTarget(xp, yp);
      }
    }
    for (int i = 0; i < _controls.size(); i++) {
      Control control = _controls.get(i);
      if (control.contains(_game, x, y)) {
        control.setPressed(true);
      }
    }
  }
  
  @Override
  public void mouseDragged(int startX, int startY, int endX, int endY) {
    if (_trackingUpdateCount >= 30) {
      _trackingUpdateCount = 0;
      int xp = (int) (endX - _xOffs * TILE_SIZE);
      int yp = (int) (endY - _yOffs * TILE_SIZE);
      xp /= TILE_SIZE;
      yp /= TILE_SIZE;
      if (_selected != null) {
        Actor target = _level.getActorAt(xp, yp, _selected);
        if (target != null) {
          _selected.setTargetActor(target);
        } else {
          _selected.moveToTarget(xp, yp);
        }
      }
    }
  }
  
  @Override
  public void mouseMoved(int startX, int startY, int endX, int endY) {
  }
  
  @Override
  public void mouseUp(int x, int y) {
    resetControls();
  }
  
  public void resetControls() {
    for (int i = 0; i < _controls.size(); i++) {
      Control control = _controls.get(i);
      if (control.isPressed()) {
        control.setPressed(false);
      }
    }
  }
  
  @Override
  public void preEnter() {
    _running = true;
    _level = new Level(this, _width, _height, _numMinRooms, _numMaxRooms, _spawnRates);
    _selected = new Actor(this, "leader", 0, 0, 0, 4, 4);
    _level.addActor(_selected);
    _level.setLeader(_selected);
    
    _playing = true;
    _xOffs = (_game.getWidth() / 2 / TILE_SIZE - _selected.getFX());
    _yOffs = (_game.getHeight() / 2 / TILE_SIZE - _selected.getFY());
    
  }
  
  @Override
  public void setup(StateBasedGame game, int screenWidth, int screenHeight) {
    _game = game;
    
  }
}