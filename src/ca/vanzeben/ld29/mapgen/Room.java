package ca.vanzeben.ld29.mapgen;

import java.util.ArrayList;

public class Room {
  private int                 _id;
  private ArrayList<RoomLink> _roomLinks  = new ArrayList<RoomLink>();
  private int                 _x;
  private int                 _y;
  private boolean             _discovered;
  private int                 _templateId = -1;
  
  public Room(int id, int x, int y, int templateId) {
    _id = id;
    _x = x;
    _y = y;
    _templateId = templateId;
  }
  
  public Room(Room parent, int id, int x, int y, int doorX, int doorY, int templateId) {
    this(id, x, y, templateId);
    addLink(parent, doorX, doorY);
    parent.addLink(this, doorX, doorY);
  }
  
  public int getTemplateId() {
    return _templateId;
  }
  
  public int getId() {
    return _id;
  }
  
  public ArrayList<RoomLink> getRoomLinks() {
    return _roomLinks;
  }
  
  public int getX() {
    return _x;
  }
  
  public int getY() {
    return _y;
  }
  
  public void discover() {
    _discovered = true;
    
  }
  
  public boolean isDiscovered() {
    return _discovered;
  }
  
  public void addLink(Room source, int doorX, int doorY) {
    _roomLinks.add(new RoomLink(source, doorX, doorY));
  }
  
  public int[] getDoorToRoom(Room other) {
    for (int i = 0; i < _roomLinks.size(); i++) {
      RoomLink link = (RoomLink) _roomLinks.get(i);
      if (link._source == other) { return link._door; }
    }
    return null;
  }
  
  private class RoomLink {
    private Room  _source;
    private int[] _door;
    
    public RoomLink(Room source, int doorX, int doorY) {
      _source = source;
      _door = new int[] { doorX, doorY };
    }
  }
  
  public ArrayList<Room> find(Room target) {
    ArrayList<Room> retVal = find(null, target, new ArrayList<Room>(), new ArrayList<Room>());
    return retVal;
  }
  
  public ArrayList<Room> find(ArrayList<Room> best, Room target, ArrayList<Room> visited, ArrayList<Room> path) {
    if (visited.contains(this)) { return best; }
    visited.add(this);
    if (this == target) {
      if (best == null || path.size() < best.size() - 1) {
        ArrayList<Room> newBest = new ArrayList<Room>(path);
        newBest.add(this);
        return newBest;
      }
      return best;
    }
    if (best != null && path.size() >= best.size() - 1) { return best; }
    path.add(this);
    for (int i = 0; i < _roomLinks.size(); i++) {
      Room next = ((RoomLink) _roomLinks.get(i))._source;
      best = next.find(best, target, visited, path);
    }
    path.remove(this);
    return best;
  }
  
  public ArrayList<Room> findNext(ArrayList<Room> ignoreList) {
    return findNext(null, ignoreList, new ArrayList<Room>(), new ArrayList<Room>());
  }
  
  public ArrayList<Room> findNext(ArrayList<Room> best, ArrayList<Room> ignoreList, ArrayList<Room> visited, ArrayList<Room> path) {
    if (visited.contains(this)) { return best; }
    visited.add(this);
    if (!isDiscovered()) {
      if (best == null || path.size() < best.size() - 1) {
        ArrayList<Room> newBest = new ArrayList<Room>(path);
        newBest.add(this);
        return newBest;
      }
      return best;
    }
    if (best != null && path.size() >= best.size() - 1) { return best; }
    path.add(this);
    for (int i = 0; i < _roomLinks.size(); i++) {
      Room next = ((RoomLink) _roomLinks.get(i))._source;
      best = next.findNext(best, ignoreList, visited, path);
    }
    path.remove(this);
    return best;
  }
}
