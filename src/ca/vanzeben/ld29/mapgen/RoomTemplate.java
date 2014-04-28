package ca.vanzeben.ld29.mapgen;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.newdawn.gdx.internal.Files;

import ca.vanzeben.ld29.env.Level;

import com.badlogic.gdx.files.FileHandle;

public class RoomTemplate {
  private static String          C_MAPSTART   = "MAPSTART";
  private static String          C_MAPEND     = "MAPEND";
  private static String          C_SHAPESTART = "SHAPESTART";
  private static String          C_SHAPEEND   = "SHAPEEND";
  private static String          C_SEP        = "|";
  private int                    _id;
  private HashMap<String, Shape> _shapes      = new HashMap<String, Shape>();
  private int[][]                _map;
  private int                    _width;
  private int                    _height;
  private String                 _path;
  
  public RoomTemplate(String filename, int id) {
    _path = filename;
    load(filename);
    _id = id;
  }
  
  public RoomTemplate() {
  }
  
  public String getPath() {
    return _path;
  }
  
  public int getId() {
    return _id;
  }
  
  private void load(String filename) {
    loadShapes(Files.get(filename));
    loadMap(Files.get(filename));
  }
  
  private void loadShapes(FileHandle handle) {
    try {
      InputStream fileIn = handle.read();
      
      Scanner scan = new Scanner(fileIn);
      scan.useDelim(C_SEP);
      
      String line = scan.nextLine();
      while (!line.equals(C_SHAPESTART)) {
        line = scan.nextLine();
      }
      
      line = scan.nextLine();
      while (!line.equals(C_SHAPEEND)) {
        Scanner scan2 = new Scanner(line);
        scan2.useDelim(C_SEP);
        String name = scan2.next();
        String id = scan2.next();
        scan2.close();
        
        Shape newShape = new Shape(name, id);
        // System.out.println("Loaded Shape: " + name + " with id of " + id);
        _shapes.put(id, newShape);
        line = scan.nextLine();
      }
      scan.close();
      fileIn.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void loadMap(FileHandle handle) {
    try {
      InputStream fileIn = handle.read();
      
      Scanner scan = new Scanner(fileIn);
      scan.useDelim(C_SEP);
      
      int x = 0;
      int y = 0;
      
      while (!scan.nextLine().equals(C_MAPSTART)) {
      }
      while (!scan.nextLine().equals(C_MAPEND)) {
        y++;
      }
      scan.close();
      fileIn.close();
      fileIn = handle.read();
      
      scan = new Scanner(fileIn);
      while (!scan.nextLine().equals(C_MAPSTART)) {
      }
      
      Scanner scan2 = new Scanner(scan.nextLine());
      scan2.useDelim(C_SEP);
      
      while (scan2.hasNext()) {
        x++;
        scan2.next();
      }
      scan2.close();
      
      _map = new int[x][y];
      // System.out.println("Loading Room Template: " + x + "x" + y);
      _width = x;
      _height = y;
      
      fileIn.close();
      scan.close();
      x = 0;
      y = 0;
      
      fileIn = handle.read();
      scan = new Scanner(fileIn);
      scan.useDelim(C_SEP);
      
      while (!scan.nextLine().equals(C_MAPSTART)) {
      }
      
      String line = scan.nextLine();
      while (!line.equals(C_MAPEND)) {
        x = 0;
        scan2 = new Scanner(line);
        scan2.useDelim(C_SEP);
        while (scan2.hasNext()) {
          int id = Integer.parseInt(((Shape) _shapes.get(scan2.next()))._id);
          _map[x][y] = id;
          x++;
        }
        
        scan2.close();
        line = scan.nextLine();
        y++;
      }
      scan.close();
      fileIn.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public RoomTemplate rotate() {
    RoomTemplate temp = new RoomTemplate();
    temp._id = _id + 1;
    temp._path = _path;
    temp._width = _width;
    temp._height = _height;
    temp._map = new int[_width][_height];
    temp._shapes = _shapes;
    for (int x = 0; x < _width; x++) {
      for (int y = 0; y < _height; y++) {
        temp._map[x][y] = _map[x][_height - 1 - y];
      }
    }
    return temp;
  }
  
  public int getWidth() {
    return _width;
  }
  
  public int getHeight() {
    return _height;
  }
  
  public boolean isClear(int xp, int yp, Level level, int[] door) {
    int doubleCount = 0;
    for (int x = 0; x < getWidth(); x++) {
      for (int y = 0; y < getHeight(); y++) {
        if (_map[x][y] != 0) {
          if (!level.validLocation(xp + x, yp + y)) { return false; }
          int tile = level.getTile(x + xp, y + yp);
          if ((tile != 0) && (tile != 1) && (!level.isDoor(tile))) { return false; }
        }
        if ((x + xp != door[0]) && (y + yp != door[1]) && _map[x][y] == 1) {
          int tile = level.getTile(x + xp, y + yp - 1);
          if (tile == 1) {
            doubleCount++;
          }
          tile = level.getTile(x + xp, y + yp + 1);
          if (tile == 1) {
            doubleCount++;
          }
          tile = level.getTile(x + xp - 1, y + yp);
          if (tile == 1) {
            doubleCount++;
          }
          
          tile = level.getTile(x + xp + 1, y + yp);
          if (tile == 1) {
            doubleCount++;
          }
        }
      }
    }
    if (doubleCount > 2) { return false; }
    return true;
  }
  
  public boolean validLocation(int x, int y) {
    return (x >= 0) && (y >= 0) && (x < _width) && (y < _height);
  }
  
  public boolean locationClear(int x, int y) {
    if (!validLocation(x, y)) { return true; }
    return _map[x][y] == 0;
  }
  
  public ArrayList<int[]> getAllOpenDoors(Level level) {
    ArrayList<int[]> result = new ArrayList<int[]>();
    for (int x = 0; x < _width; x++) {
      for (int y = 0; y < _height; y++) {
        for (int tx = -1; tx < 2; tx++) {
          for (int ty = -1; ty < 2; ty++) {
            if (tx == 0 || ty == 0) {
              continue;
            }
            if ((level.isDoor(_map[x][y])) && (locationClear(x + tx, y + ty))) {
              result.add(new int[] { x, y });
            }
          }
        }
      }
    }
    return result;
  }
  
  public ArrayList<int[]> findDoorsWithSpace(Level level, int tx, int ty) {
    ArrayList<int[]> result = new ArrayList<int[]>();
    for (int x = 0; x < _width; x++) {
      for (int y = 0; y < _height; y++) {
        if ((level.isDoor(_map[x][y])) && (locationClear(x + tx, y + ty))) {
          result.add(new int[] { x, y });
        }
      }
    }
    return result;
  }
  
  public void place(Random random, int xp, int yp, Level level, boolean b, Room room) {
    for (int x = 0; x < _width; x++) {
      for (int y = 0; y < _height; y++) {
        if (_map[x][y] != 0) {
          level.setRoom(xp + x, yp + y, room);
          level.setTile(xp + x, yp + y, _map[x][y]);
        }
      }
    }
  }
  
  private class Shape {
    private String _name;
    private String _id;
    
    public Shape(String name, String id) {
      _name = name;
      _id = id;
    }
    
  }
  
}
