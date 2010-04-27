package coreutilities.samples;

import coreutilities.gui.TransparentJWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class TranspWinImplementation
{
  private static final TransparentJWindow transpWin = new TransparentJWindow()
    {
      protected void transparentWindowPaintComponent(Graphics g)
      {
        g.setColor(new Color(5, 5, 5, 50));
        g.fillOval(0, 0, this.getSize().width, this.getSize().height);
//      g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        g.setColor(Color.blue);
        g.drawString("Akeu Wowowow!", 50, 50);
      }
      
      protected void onClick()
      {
        manageClick();
      }
    };

  public static void main(String[] args)
  {
    try
    {
      JButton b = new JButton("Test");
      b.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            JOptionPane.showMessageDialog(transpWin, "Ailleu!", "Bing", JOptionPane.WARNING_MESSAGE);
          }
        });
      transpWin.getContentPane().add(new JLabel("Test Label"));
      transpWin.getContentPane().add(b);

      transpWin.setBounds(transpWin.getCurrentPosition().x, 
                          transpWin.getCurrentPosition().y, 
                          transpWin.getWinDim().width, 
                          transpWin.getWinDim().height);
      transpWin.setVisible(true);        
      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private static void manageClick()
  {
    int resp = JOptionPane.showConfirmDialog(transpWin, "Exit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (resp == JOptionPane.YES_OPTION)
      System.exit(0);
    else
      System.out.println("Still alive!");
  }
}
