package ca.vanzeben.ld29.ent;

public abstract interface TileEntity {
  public abstract int getX();
  
  public abstract int getY();
  
  public abstract int getCurrentFrameId();
  
  public abstract void update();
  
  public abstract boolean complete();
  
  public abstract boolean requiresDiscovery();
}
