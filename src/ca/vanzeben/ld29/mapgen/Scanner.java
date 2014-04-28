package ca.vanzeben.ld29.mapgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Scanner {
  private String          _delim;
  private StringTokenizer _tokens;
  private BufferedReader  _reader;
  private String          _current;
  
  public Scanner(InputStream fileIn) {
    _reader = new BufferedReader(new InputStreamReader(fileIn));
  }
  
  public Scanner(String line) {
    setLine(line);
  }
  
  public void useDelim(String delim) {
    _delim = delim;
    setLine(_current);
  }
  
  public String nextLine() {
    try {
      String line = _reader.readLine();
      setLine(line);
      return line;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public void setLine(String line) {
    _current = line;
    if (line != null) {
      _tokens = new StringTokenizer(line, _delim);
    } else {
      _tokens = new StringTokenizer("");
    }
  }
  
  public boolean hasNext() {
    return _tokens.hasMoreTokens();
  }
  
  public String next() {
    return _tokens.nextToken();
  }
  
  public void close() {
    if (_reader != null) {
      try {
        _reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
