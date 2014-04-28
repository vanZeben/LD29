package ca.vanzeben.ld29.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.newdawn.gdx.path.AStarPathFinder;
import org.newdawn.gdx.path.Path;
import org.newdawn.gdx.path.PathFinder;
import org.newdawn.gdx.path.PathFinderMap;
import org.newdawn.gdx.path.PathMover;

import ca.vanzeben.ld29.Resources;
import ca.vanzeben.ld29.ent.Actor;
import ca.vanzeben.ld29.ent.TileEntity;
import ca.vanzeben.ld29.ent.objectives.BrainObjective;
import ca.vanzeben.ld29.mapgen.Room;
import ca.vanzeben.ld29.mapgen.RoomTemplate;
import ca.vanzeben.ld29.states.InGameState;

public class Level implements PathFinderMap {
  
  private static final int                   EMPTY                  = 0;
  private static final int                   WALL                   = 1;
  private static final int                   FLOOR                  = 2;
  private static final int                   DOOR                   = 3;
  private static final int                   BRAIN                  = 9;
  
  private static final int                   NORTH                  = 1;
  private static final int                   SOUTH                  = 2;
  private static final int                   EAST                   = 3;
  private static final int                   WEST                   = 4;
  
  public static final int                    FLOOR_LAYER            = 0;
  public static final int                    WALL_LAYER_1           = 1;
  public static final int                    WALL_LAYER_2           = 2;
  public static final int                    DOOR_LAYER             = 3;
  private static final int                   ENEMY_COUNT            = 7;
  private static final int                   BLOOD_CELL_COUNT       = 5;
  
  private InGameState                        _state;
  private int[][]                            _map;
  private int[][][]                          _renderedMap;
  private boolean[][]                        _discoveredMap;
  private int                                _width;
  private int                                _height;
  private Room                               _rootRoom;
  private Room[][]                           _rooms;
  
  private int                                _floorBase             = 32;
  private int                                _wallBase              = 0;
  
  private ArrayList<Actor>                   _actors                = new ArrayList<Actor>();
  private PathFinder                         _finder;
  private Actor                              _pathFindingActor      = null;
  private HashMap<Room, HashMap<Room, Room>> _nextSteps             = new HashMap<Room, HashMap<Room, Room>>();
  private boolean                            _pathFindingWithActors = false;
  private Actor                              _leader;
  private List<TileEntity>                   _tileEntities          = new ArrayList<TileEntity>();
  
  private Random                             random                 = new Random();
  private int                                _spawnRates            = 0;
  private int                                _initalBloodCellCount  = 0;
  
  public Level(InGameState state, int width, int height, int minRooms, int maxRooms, int spawnRates) {
    _state = state;
    _width = width;
    _height = height;
    _spawnRates = spawnRates;
    generateMap(random, minRooms, maxRooms);
    renderMap(state);
    // displayDebugMap();
    discover(4, 4);
  }
  
  public void addTileEntity(TileEntity e) {
    _tileEntities.add(e);
  }
  
  public TileEntity getTileEntityAt(int x, int y) {
    List<TileEntity> entities = getTileEntities();
    for (int i = 0; i < entities.size(); i++) {
      TileEntity e = entities.get(i);
      if (e.getX() == x && e.getY() == y) { return e; }
    }
    return null;
  }
  
  public List<TileEntity> getTileEntities() {
    return _tileEntities;
  }
  
  private boolean blocksView(int x, int y) {
    switch (getTile(x, y)) {
      case 1:
        return true;
    }
    return false;
  }
  
  public Actor getLeader() {
    return _leader;
  }
  
  public void setLeader(Actor leader) {
    _leader = leader;
  }
  
  public ArrayList<Actor> getTeam() {
    ArrayList<Actor> retVal = new ArrayList<Actor>();
    ArrayList<Actor> actors = _actors;
    for (int i = 0; i < actors.size(); i++) {
      Actor a = actors.get(i);
      if (a.getTeam() == 1 || a.getTeam() == 0) {
        retVal.add(a);
      }
    }
    return retVal;
  }
  
  private void clearAndSet() {
    _finder = new AStarPathFinder(this);
    
    _map = new int[_width][_height];
    _renderedMap = new int[_width][_height][10];
    _discoveredMap = new boolean[_width][_height];
    _rooms = new Room[_width][_height];
    _tileEntities = new ArrayList<TileEntity>();
    _actors = new ArrayList<Actor>();
    _pathFindingActor = null;
    _nextSteps = new HashMap<Room, HashMap<Room, Room>>();
    _pathFindingWithActors = false;
    
  }
  
  private void generateMap(Random random, int minRooms, int maxRooms) {
    int placedCount = 0;
    int numAttempts = 0;
    int maxAttempts = 10;
    while (_tileEntities.size() <= 0) {
      if ((numAttempts >= maxAttempts && _tileEntities.size() >= 1) || (_tileEntities.size() >= 1 && placedCount >= minRooms)) {
        break;
      }
      numAttempts++;
      clearAndSet();
      RoomTemplate template = Resources.TEMPLATES.get(0);
      if (template.isClear(0, 0, this, new int[] { -1, -1 })) {
        _rootRoom = new Room(1, 4, 4, 0);
        _rootRoom.discover();
        template.place(random, 0, 0, this, false, _rootRoom);
      }
      placedCount = 0;
      int id = 2;
      Room farthestRoom = null;
      int loc = random.nextInt(3);
      inner: for (int i = 0; i < 500; i++) {
        if (placedCount >= maxRooms) {
          break inner;
        }
        int door[] = findRandomDoor(random);
        if (door == null) {
          break;
        }
        int tx = 0;
        int ty = 0;
        Room source = getRoom(door[0], door[1]);
        if (isClear(door[0], door[1] - 1)) {
          tx = 0;
          ty = 1;
        }
        if (isClear(door[0], door[1] + 1)) {
          tx = 0;
          ty = -1;
        }
        if (isClear(door[0] - 1, door[1])) {
          tx = 1;
          ty = 0;
        }
        if (isClear(door[0] + 1, door[1])) {
          tx = -1;
          ty = 0;
        }
        boolean placed = false;
        if (tx != 0 || ty != 0) {
          ArrayList<RoomTemplate> rooms = Resources.TEMPLATES.getAll();
          ArrayList<int[]> doorOptions;
          
          while (rooms.size() > 0 && !placed) {
            RoomTemplate option = (RoomTemplate) rooms.remove(random.nextInt(rooms.size()));
            doorOptions = option.findDoorsWithSpace(this, tx, ty);
            if (doorOptions.size() > 0 && !placed) {
              int[] doorOption = (int[]) doorOptions.remove(random.nextInt(doorOptions.size()));
              if (option.isClear(door[0] - doorOption[0], door[1] - doorOption[1], this, door)) {
                Room room = new Room(source, id, door[0] - tx * 2, door[1] - ty * 2, door[0], door[1], option.getId());
                if (farthestRoom == null || ((loc == 0 || loc == 1) && room.getY() >= farthestRoom.getY() && room.getX() >= farthestRoom.getX())
                    || ((loc == 2 || loc == 0) && room.getY() <= farthestRoom.getY() && room.getX() >= farthestRoom.getX()) && option.getAllOpenDoors(this).size() > 0) {
                  farthestRoom = room;
                }
                if ((100D / (double) random.nextInt(ENEMY_COUNT)) <= (_spawnRates + 1 * 33) && random.nextBoolean()) {
                  addActor(new Actor(_state, "base_enemy", 2, 0, 3, door[0] - tx * 2, door[1] - ty * 2));
                }
                if ((100D / (double) random.nextInt(BLOOD_CELL_COUNT)) <= (_spawnRates + 2 * 33)) {
                  addActor(new Actor(_state, "dummy", 4, 0, 2, door[0] - tx * 2, door[1] - ty * 2));
                  _initalBloodCellCount++;
                }
                
                option.place(random, door[0] - doorOption[0], door[1] - doorOption[1], this, false, room);
                placed = true;
              }
            }
          }
        }
        if (!placed) {
          setTile(door[0], door[1], 1);
        } else {
          placedCount++;
          id++;
        }
      }
      if (farthestRoom != null) {
        RoomTemplate temp = Resources.TEMPLATES.get(farthestRoom.getTemplateId());
        ArrayList<RoomTemplate> objectives = Resources.TEMPLATES.getObjectiveRooms();
        ArrayList<int[]> placedDoors = temp.getAllOpenDoors(this);
        
        if (temp != null && objectives != null) {
          outer: for (int j = 0; j < placedDoors.size(); j++) {
            int[] door = placedDoors.get(j);
            int openX = 0;
            int openY = 0;
            checker: for (int xx = -1; xx < 2; xx++) {
              for (int yy = -1; yy < 2; yy++) {
                if (xx == 0 || yy == 0) {
                  continue;
                }
                if ((temp.locationClear(door[0] + xx, door[1] + yy))) {
                  openX = xx;
                  openY = yy;
                  break checker;
                }
              }
            }
            for (int i = 0; i < objectives.size(); i++) {
              RoomTemplate obj = objectives.get(i);
              ArrayList<int[]> doorOptions = obj.findDoorsWithSpace(this, openX, openY);
              if (doorOptions.size() > 0) {
                int[] doorOption = (int[]) doorOptions.remove(random.nextInt(doorOptions.size()));
                if (obj.isClear(door[0] - doorOption[0], door[1] - doorOption[1], this, door)) {
                  Room room = new Room(farthestRoom, id, door[0] - openX * 2, door[1] - openY * 2, door[0], door[1], obj.getId());
                  
                  obj.place(random, door[0] + doorOption[0], door[1] + doorOption[1], this, true, room);
                  break outer;
                }
              }
            }
          }
        }
      }
      
      for (int x = 0; x < _width; x++) {
        for (int y = 0; y < _height; y++) {
          if (isDoor(getTile(x, y))) {
            int floorCount = 0;
            floorCount += (getTile(x, y - 1) == 2) ? 1 : 0;
            floorCount += (getTile(x, y + 1) == 2) ? 1 : 0;
            floorCount += (getTile(x - 1, y) == 2) ? 1 : 0;
            floorCount += (getTile(x + 1, y) == 2) ? 1 : 0;
            if (floorCount != 2) {
              setTile(x, y, 1);
            }
          }
        }
      }
    }
  }
  
  public void attemptWin() {
    int numAllies = this.getAllies().size();
    boolean hasWon = false;
    switch (_spawnRates) {
      case 0:
        hasWon = ((double) numAllies / (double) _initalBloodCellCount * 100D) >= 10.0D;
        break;
      case 1:
        hasWon = ((double) numAllies / (double) _initalBloodCellCount * 100D) >= 15.0D;
        break;
      case 2:
        hasWon = ((double) numAllies / (double) _initalBloodCellCount * 100D) >= 30.0D;
        break;
    }
    if (hasWon) {
      _state.win(((double) numAllies / (double) _initalBloodCellCount * 100D));
    } else {
      _state.needMoreBlood(((double) numAllies / (double) _initalBloodCellCount * 100D), (_spawnRates == 0 ? 10 : (_spawnRates == 1 ? 15 : (_spawnRates == 2 ? 30 : 0))));
    }
  }
  
  public void displayDebugMap() {
    System.out.println("----START----");
    for (int y = 0; y < _height; y++) {
      for (int x = 0; x < _width; x++) {
        for (int i = 0; i < 10; i++) {
          int render = getRender(x, y, i);
          int tile = getTile(x, y);
          if (render > -1 || tile == 0) {
            System.out.print("[" + tile + "|" + (render < 10 && render > -1 ? "0" : "") + render + "]");
          }
        }
      }
      System.out.println("");
    }
    System.out.println("----END----");
  }
  
  public void setTile(int x, int y, int id) {
    if (notValidLocation(x, y)) { return; }
    if (id == BRAIN && !hasBrain()) {
      addTileEntity(new BrainObjective(this, x, y));
      id = FLOOR;
    }
    _map[x][y] = id;
  }
  
  private boolean hasBrain() {
    List<TileEntity> tiles = _tileEntities;
    for (int i = 0; i < tiles.size(); i++) {
      if (tiles.get(i) instanceof BrainObjective) { return true; }
    }
    return false;
  }
  
  private boolean isClear(int x, int y) {
    if (notValidLocation(x, y)) { return false; }
    return _map[x][y] == 0;
  }
  
  private Room getRoom(int x, int y) {
    if (notValidLocation(x, y)) { return null; }
    return _rooms[x][y];
  }
  
  private int[] findRandomDoor(Random random) {
    ArrayList<int[]> doors = new ArrayList<int[]>();
    for (int x = 0; x < _width; x++) {
      for (int y = 0; y < _height; y++) {
        if ((isDoor(getTile(x, y))) && ((getTile(x, y - 1) == 0) || (getTile(x, y + 1) == 0) || (getTile(x - 1, y) == 0) || (getTile(x + 1, y) == 0))) {
          doors.add(new int[] { x, y });
        }
      }
    }
    if (doors.size() == 0) { return null; }
    return (int[]) doors.get(random.nextInt(doors.size()));
  }
  
  public void draw() {
    renderMap(_state);
  }
  
  public void renderMap(InGameState state) {
    int wallCount = 0;
    int floorCount = 0;
    for (int y = 0; y < _height; y++) {
      for (int x = 0; x < _width; x++) {
        for (int i = 0; i < 10; i++) {
          setRender(x, y, i, -1);
        }
        if (hasFloor(x, y - 1)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 0);
        } else if ((hasFloor(x, y - 1) || hasFloor(x, y + 1)) && hasFloor(x - 1, y) && hasFloor(x + 1, y)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 5);
        } else if ((hasFloor(x - 1, y) || hasFloor(x + 1, y)) && hasFloor(x, y - 1) && hasFloor(x, y + 1)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 7);
        } else if (hasFloor(x, y - 1)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 2);
        } else if (hasFloor(x, y + 1)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 1);
        } else if (hasFloor(x - 1, y) && hasFloor(x + 1, y)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 5);
        } else if (hasFloor(x - 1, y)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 4);
        } else if (hasFloor(x + 1, y)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 3);
        }
        
        if (hasFloor(x, y)) {
          setRender(x, y, FLOOR_LAYER, _floorBase + 0);
        } else if (hasFloor(x, y + 1)) {
          if ((hasFloor(x - 1, y)) && (hasFloor(x + 1, y))) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 16);
          } else if (hasFloor(x - 1, y)) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 8);
          } else if (hasFloor(x + 1, y)) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 9);
          } else {
            wallCount++;
            setRender(x, y, WALL_LAYER_1, _wallBase + 11);
          }
        } else if (hasFloor(x, y - 1)) {
          if ((hasFloor(x - 1, y)) && (hasFloor(x + 1, y))) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 17);
          } else if (hasFloor(x - 1, y)) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 6);
          } else if (hasFloor(x + 1, y)) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 7);
          } else if (isWall(x, y + 1)) {
            if ((hasFloor(x + 1, y + 1)) && (hasFloor(x - 1, y + 1))) {
              setRender(x, y, WALL_LAYER_1, _wallBase + 29);
            } else if (hasFloor(x + 1, y + 1)) {
              setRender(x, y, WALL_LAYER_1, _wallBase + 18);
            } else if (hasFloor(x - 1, y + 1)) {
              setRender(x, y, WALL_LAYER_1, _wallBase + 19);
            } else {
              setRender(x, y, WALL_LAYER_1, _wallBase + 10);
            }
          } else {
            setRender(x, y, WALL_LAYER_1, _wallBase + 10);
          }
        } else {
          if (hasFloor(x - 1, y)) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 3);
          } else if (hasFloor(x - 1, y + 1)) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 1);
          } else if (hasFloor(x - 1, y - 1)) {
            setRender(x, y, WALL_LAYER_1, _wallBase + 5);
          }
          if (hasFloor(x + 1, y)) {
            setRender(x, y, WALL_LAYER_2, _wallBase + 2);
          } else if (hasFloor(x + 1, y + 1)) {
            setRender(x, y, WALL_LAYER_2, _wallBase + 0);
          } else if (hasFloor(x + 1, y - 1)) {
            setRender(x, y, WALL_LAYER_2, _wallBase + 4);
          }
        }
      }
    }
  }
  
  public boolean update() {
    boolean moving = false;
    for (int i = 0; i < _actors.size(); i++) {
      moving |= ((Actor) _actors.get(i)).update();
    }
    return moving;
  }
  
  @Override
  public boolean blocked(PathMover mover, PathMover obj, int sx, int sy, int x, int y, boolean ignoreActors) {
    return blocksMovement(sx, sy, _pathFindingActor, ignoreActors);
  }
  
  private boolean blocksMovement(int sx, int sy, Actor source, boolean ignoreActors) {
    boolean retVal = false;
    if (_pathFindingWithActors) {
      Actor blocker = getActorAt(sx, sy, source);
      if (blocker != null) { return true; }
    }
    switch (_map[sx][sy]) {
      case 0:
      case 1:
      case 4:
        return true;
    }
    retVal = false;
    return false;
  }
  
  @Override
  public int getHeight() {
    return _height;
  }
  
  @Override
  public PathMover getMoverAt(int arg0, int arg1) {
    return null;
  }
  
  @Override
  public int getWidth() {
    return _width;
  }
  
  @Override
  public boolean locationDiscovered(int x, int y) {
    return true;
  }
  
  @Override
  public boolean notValidLocation(int x, int y) {
    return !validLocation(x, y);
  }
  
  @Override
  public boolean validLocation(int x, int y) {
    return (x >= 0) && (y >= 0) && (x < _width) && (y < _height);
  }
  
  public int getTile(int x, int y) {
    if (notValidLocation(x, y)) { return 0; }
    return _map[x][y];
  }
  
  private boolean isDoor(int x, int y) {
    return getTile(x, y) == DOOR;
  }
  
  public boolean hasFloor(int x, int y) {
    return getTile(x, y) == FLOOR || isDoor(x, y);
  }
  
  public boolean isDoor(int tileId) {
    return tileId == DOOR;
  }
  
  public boolean isWall(int x, int y) {
    return getTile(x, y) == WALL;
  }
  
  public void setRoom(int x, int y, Room room) {
    if (notValidLocation(x, y)) { return; }
    _rooms[x][y] = room;
  }
  
  public void setRender(int x, int y, int layer, int tile) {
    if (notValidLocation(x, y)) { return; }
    _renderedMap[x][y][layer] = tile;
  }
  
  private int getRenderID(int x, int y, int layer) {
    if (notValidLocation(x, y)) { return -1; }
    return _renderedMap[x][y][layer];
  }
  
  public int getRender(int x, int y, int layer) {
    if (notValidLocation(x, y)) { return -1; }
    return getRenderID(x, y, layer);
  }
  
  public void addActor(Actor actor) {
    actor.setLevel(this);
    _actors.add(actor);
  }
  
  public void removeActor(Actor actor) {
    _actors.remove(actor);
  }
  
  public ArrayList<Actor> getActors() {
    return _actors;
  }
  
  public List<Actor> getAllies() {
    ArrayList<Actor> allies = new ArrayList<Actor>();
    for (Actor a : getActors()) {
      if (a.getTeam() == 1) {
        allies.add(a);
      }
    }
    return allies;
  }
  
  public Actor getActorAt(int tx, int ty, Actor excludingActor) {
    for (int i = 0; i < _actors.size(); i++) {
      Actor actor = (Actor) _actors.get(i);
      if (actor != excludingActor && actor.isAt(tx, ty)) { return actor; }
    }
    return null;
  }
  
  public void actorMovedToCoord(Actor actor, int x, int y) {
    if (actor.getTeam() <= 1) {
      discover(x, y);
    }
  }
  
  public boolean isDiscovered(int x, int y) {
    if (notValidLocation(x, y)) { return false; }
    
    return _discoveredMap[x][y];
  }
  
  public void discover(int x, int y) {
    if (notValidLocation(x, y)) { return; }
    if (_discoveredMap[x][y]) { return; }
    if ((getTile(x, y) == 2) && (_rooms[x][y] != null)) {
      _rooms[x][y].discover();
    }
    _discoveredMap[x][y] = true;
    Actor target = getActorAt(x, y, null);
    if (target != null) {
      target.activate();
    }
    if (blocksView(x, y)) { return; }
    for (int xp = -1; xp < 2; xp++) {
      for (int yp = -1; yp < 2; yp++) {
        if ((xp != 0) || (yp != 0)) {
          discover(x + xp, y + yp);
        }
      }
    }
  }
  
  public Path findPath(int tx, int ty, Actor actor) {
    if (getTile(tx, ty) == 0) { return null; }
    Room source = getRoom(actor.getX(), actor.getY());
    Room target = getRoom(tx, ty);
    if (source == null || target == null) { return null; }
    if (source != target) {
      Room nextStep = getNextStep(source, target);
      if (nextStep != null) {
        int door[] = source.getDoorToRoom(nextStep);
        if (actor.getX() == door[0] && actor.getY() == door[1]) {
          Room secondStep = getNextStep(nextStep, target);
          if (secondStep != null) {
            door = nextStep.getDoorToRoom(secondStep);
            tx = door[0];
            ty = door[1];
          }
        } else {
          tx = door[0];
          ty = door[1];
        }
      } else {
        return null;
      }
    }
    _finder.clear();
    _pathFindingActor = actor;
    
    Path path = null;
    _pathFindingWithActors = true;
    path = _finder.findPath(actor, tx, ty, 15, false, false);
    
    if (path == null) {
      _pathFindingWithActors = false;
      path = _finder.findPath(actor, tx, ty, 15, false, false);
    }
    return path;
  }
  
  private Room getNextStep(Room source, Room target) {
    HashMap<Room, Room> cachedStep = (HashMap<Room, Room>) _nextSteps.get(source);
    if (cachedStep == null) {
      cachedStep = new HashMap<Room, Room>();
      _nextSteps.put(source, cachedStep);
    }
    Room cached = (Room) cachedStep.get(target);
    if (cached == null) {
      ArrayList<Room> pathToRoom = source.find(target);
      if (pathToRoom.size() > 1) {
        cached = (Room) pathToRoom.get(1);
        cachedStep.put(target, cached);
      }
    }
    return cached;
  }
  
  public boolean hasLOS(float x1, float y1, float x2, float y2) {
    x1 = (int) (x1 * 2.0F) / 2.0F;
    y1 = (int) (y1 * 2.0F) / 2.0F;
    x2 = (int) (x2 * 2.0F) / 2.0F;
    y2 = (int) (y2 * 2.0F) / 2.0F;
    if (hasLOSFixed(x1, y1, x2, y2)) { return true; }
    return false;
  }
  
  private boolean hasLOSFixed(float x1, float y1, float x2, float y2) {
    float dx = x2 - x1;
    float dy = y2 - y1;
    float len = Math.max(Math.abs(dx), Math.abs(dy)) * 2.0F;
    
    dx /= len;
    dy /= len;
    for (int i = 0; i < len; i++) {
      if (_map[((int) x1)][((int) y1)] == 1) { return false; }
      x1 += dx;
      y1 += dy;
    }
    return true;
  }
  
}
