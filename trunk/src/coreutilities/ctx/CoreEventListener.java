package coreutilities.ctx;

import java.util.ArrayList;
import java.util.EventListener;

public abstract class CoreEventListener implements EventListener 
{
  public void updateCompleted(ArrayList<String> fList) {}
  public void networkOk(boolean b) {}
}
