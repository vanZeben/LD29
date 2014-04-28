package ca.vanzeben.ld29.mapgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomTemplates {
  private List<RoomTemplate> _templates = new ArrayList<RoomTemplate>();
  
  public RoomTemplates() {
    add(new RoomTemplate("data/rooms/basic0.map", _templates.size()), 1);
    add(new RoomTemplate("data/rooms/basic1.map", _templates.size()), 1);
    add(new RoomTemplate("data/rooms/corridor0.map", _templates.size()), 2);
    add(new RoomTemplate("data/rooms/corridor1.map", _templates.size()), 2);
    add(new RoomTemplate("data/rooms/large0.map", _templates.size()), 3);
    add(new RoomTemplate("data/rooms/brain_objective.map", _templates.size()), 1);
  }
  
  public void add(RoomTemplate template, int weight) {
    for (int i = 0; i < weight; i++) {
      _templates.add(template);
      _templates.add(template.rotate());
      _templates.add(template.rotate().rotate());
      _templates.add(template.rotate().rotate().rotate());
    }
  }
  
  public RoomTemplate get(int i) {
    return _templates.get(i);
  }
  
  public RoomTemplate getRandom(Random random) {
    return get(random.nextInt(getSize()));
  }
  
  public ArrayList<RoomTemplate> getObjectiveRooms() {
    ArrayList<RoomTemplate> retVal = new ArrayList<RoomTemplate>(_templates);
    for (int i = 0; i < retVal.size(); i++) {
      int pick = (int) (Math.random() * retVal.size());
      RoomTemplate room = retVal.remove(pick);
      if (room.getPath().toLowerCase().contains("objective")) {
        retVal.add(room);
      }
    }
    return retVal;
  }
  
  public int getSize() {
    return _templates.size();
  }
  
  public ArrayList<RoomTemplate> getAll() {
    ArrayList<RoomTemplate> retVal = new ArrayList<RoomTemplate>(_templates);
    for (int i = 0; i < retVal.size(); i++) {
      int pick = (int) (Math.random() * retVal.size());
      RoomTemplate room = retVal.remove(pick);
      if (!room.getPath().toLowerCase().contains("objective")) {
        retVal.add(room);
      }
    }
    return retVal;
  }
}
