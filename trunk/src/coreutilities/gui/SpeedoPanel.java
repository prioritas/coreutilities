package coreutilities.gui;


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class SpeedoPanel
  extends JPanel
{
  @SuppressWarnings("compatibility:-5990585320126728229")
  public final static long serialVersionUID = 1L;
  
  private final static boolean GLOSSY_DISPLAY = true;
  private final static boolean WITH_TEXTURE = false;
  private final static boolean WITH_BEVEL_SHADE = true;
  
  private final static double EXTERNAL_RADIUS_COEFF = 1.050;
  private final static double INTERNAL_RADIUS_COEFF = 1.025;
  
  private double speed = 0D;
  private double prevSpeed = 0;
  
  private boolean withMinMax = true;
  
  private double minimumSpeed = Double.MAX_VALUE;
  private double maximumSpeed = -minimumSpeed;
  
  private final static int DISPLAY_OFFSET = 5;
  
  private final static double INCREMENT_DEFAULT_VALUE = 0.25;
  private final static int TICK_DEFAULT_VALUE = 5;
  
  private double maxSpeed = 50D;
  private double increment = 0.25;
  private int bigTick = 5;        

  private double speedUnitRatio = (180D - (2 * DISPLAY_OFFSET)) / maxSpeed;

  // The background font is transparent
  private final Color bgColor = new Color((Color.gray.getRed()/255f), (Color.gray.getGreen()/255f), (Color.gray.getBlue()/255f), 0.5f);

  public void setLabel(String label)
  {
    this.label = label;
  }

  public void setWithMinMax(boolean withMinMax)
  {
    this.withMinMax = withMinMax;
  }

  public boolean isWithMinMax()
  {
    return withMinMax;
  }

  public void resetMinMax()
  {
    minimumSpeed = Double.MAX_VALUE;
    maximumSpeed = -minimumSpeed;
  }
  
  public enum SpeedUnit
  {
    KNOT ("knots"),
    KMH  ("km/h"),
    MPH  ("mph"),
    MS   ("m/s");

    @SuppressWarnings("compatibility:-2776162247172094666")
    public final static long serialVersionUID = 1L;

    private final String label;
    SpeedUnit(String label)
    {
      this.label = label;
    }
    
    public String label() { return this.label; }
  }
  private SpeedUnit speedUnit = SpeedUnit.KNOT;
  
  private String label = "";
  
  private SpeedoPanel instance = this;
  private boolean smooth = true;
  
  private Font jumboFont = null;
  private Font bgJumboFont = null;
  
  protected transient Stroke thick = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  protected transient Stroke dotted = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {2f}, 0f);
  protected transient Stroke origStroke = null;
  
  protected int radius = 0;
  protected Point center = null;
  
  public SpeedoPanel(double s)
  {
    this(s, INCREMENT_DEFAULT_VALUE, TICK_DEFAULT_VALUE);
  }
  
  public SpeedoPanel(double s, boolean smooth)
  {
    this(s, INCREMENT_DEFAULT_VALUE, TICK_DEFAULT_VALUE, smooth);
  }
  
  public SpeedoPanel(double s, double inc, boolean smooth)
  {
    this(s, inc, TICK_DEFAULT_VALUE, smooth);
  }
  
  public SpeedoPanel(double s, double inc, int tick)
  {
    this(s, inc, tick, true);
  }
  
  public SpeedoPanel(double s, double inc, int tick, boolean smooth)
  {
    this.maxSpeed = s;
    this.increment = inc;
    this.bigTick = tick;
    this.smooth = smooth;
    
    speedUnitRatio = (180D - (2 * DISPLAY_OFFSET)) / maxSpeed;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(null);
    if (!WITH_TEXTURE)
    {
      this.setOpaque(false);
      this.setBackground(new Color(0, 0, 0, 0));
    }
//    SpeedoContext.getInstance().addApplicationListener(new SpeedoEventListener()
//      {
//        public void setSpeed(double d)
//        {
//          instance.setSpeed(d);
//        }
//      });
    
//  jumboFont = JumboDisplay.tryToLoadFont("ds-digi.ttf", null);
//  jumboFont = JumboDisplay.tryToLoadFont("CodenameCoderFree4F-Bold.ttf", this);
//  jumboFont = JumboDisplay.tryToLoadFont("AUDIMSCB.TTF", this);
    jumboFont   = JumboDisplay.tryToLoadFont("TRANA___.TTF", this);
    bgJumboFont = JumboDisplay.tryToLoadFont("TRANGA__.TTF", this);
  }
  
  public void setSpeedUnit(SpeedoPanel.SpeedUnit speedUnit)
  {
    this.speedUnit = speedUnit;
  }

  private double damping = 0.5;
  
  public void setSpeed(final double d)
  {
    this.speed = d;
    minimumSpeed = Math.min(minimumSpeed, d);
    maximumSpeed = Math.max(maximumSpeed, d);
    double from = prevSpeed; 
    double to = d;
    
    damping = Math.abs(prevSpeed - speed) / 100;
    if (damping == 0)
      damping = 0.5;
//  System.out.println("Damping:" + DAMPING);
    
    // Manage the case 350-10
    if (Math.abs(prevSpeed - speed) > 180)
    {
      if (Math.signum(Math.cos(Math.toRadians(prevSpeed))) == Math.signum(Math.cos(Math.toRadians(speed))))
      {
        if (from > to)
          to += 360;
        else
          to -= 360;
      }
    }
//  final double _to = to;
    if (from != to)
    {
      int sign = (from>to)?-1:1;
      prevSpeed = d;
      if (smooth)
      {
        // Smooth rotation
        for (double h=from; (sign==1 && h<=to) || (sign==-1 && h>=to); h+=(damping*sign))
        {
//        System.out.println("Setting speed from " + from + " to " + to + ", at " + h);
          final double _h = h;
          try
          {
            // For a smooth move of the hand
            SwingUtilities.invokeAndWait(new Runnable()
  //        SwingUtilities.invokeLater(new Runnable()
              {
                public void run()
                {
                  double _s = _h % 360;
                  while (_s < 0) _s += 360;
                  instance.speed = _s;
    //            System.out.println("-> Speed:" + speed + " (" + _to + ")");
                  instance.repaint();
                }
              });
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
      }  
      else
      {
//      instance.speed = to;
        instance.repaint();
      }
    }
  }

  private static void drawGlossyHalfCircularDisplay(Graphics2D g2d, 
                                                    Point center, 
                                                    int radius, 
                                                    Color lightColor, 
                                                    Color darkColor, 
                                                    float transparency)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);
//  g2d.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);
    g2d.fillArc(center.x - radius, center.y - radius, 2 * radius, 2 * radius, 0, 180);

    Point gradientOrigin = new Point(center.x - radius,
                                     center.y - radius);
    GradientPaint gradient = new GradientPaint(gradientOrigin.x, 
                                               gradientOrigin.y, 
                                               lightColor, 
                                               gradientOrigin.x, 
                                               gradientOrigin.y + (2 * radius / 3), 
                                               darkColor); // vertical, light on top
    g2d.setPaint(gradient);
//  g2d.fillOval((int)(center.x - (radius * 0.90)), 
//               (int)(center.y - (radius * 0.95)), 
//               (int)(2 * radius * 0.9), 
//               (int)(2 * radius * 0.95));
    g2d.fillArc((int)(center.x - (radius * 0.90)), 
                (int)(center.y - (radius * 0.95)), 
                (int)(2 * radius * 0.9), 
                (int)(2 * radius * 0.95),
                0, 
                180);
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    radius = Math.min(this.getWidth() / 2, this.getHeight());
    center = new Point(this.getWidth() / 2, this.getHeight() - 20);
    
    // For the scale and shadow:
    radius *= 0.8; // 0.9;
    
//  g.setColor(Color.lightGray);

    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);      
    origStroke = g2d.getStroke();
    
    if (GLOSSY_DISPLAY)
    {
      Color bgColor = this.getBackground();
      if (WITH_TEXTURE)
      {            
        bgColor = new Color(bgColor.getRed(),
                            bgColor.getGreen(),
                            bgColor.getBlue(),
                            10); // transparent
      }
      // Shaded bevel
      if (WITH_BEVEL_SHADE)
      {
        RadialGradientPaint rgp = new RadialGradientPaint(center, 
                                                          (int)(radius * 1.15) + 15, 
                                                          new float[] {0f, 0.85f, 1f}, 
                                                          new Color[] { bgColor, Color.black, bgColor });
        g2d.setPaint(rgp);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
      }
      // White disc scale, outside
//    g2d.setColor(Color.white);
//    int extRadius = (int)(radius * EXTERNAL_RADIUS_COEFF) + 15; // 10 is the font size, for the months (in case of map)
//    g2d.fillOval(center.x - extRadius, center.y - extRadius, 2 * extRadius, 2 * extRadius);      
      // Glossy Display
      drawGlossyHalfCircularDisplay(g2d, center, radius, Color.lightGray, Color.black, 1f);
    }
    
    g2d.setStroke(origStroke);
    drawGrid(g2d);
    drawHand(g2d);
  }
  
  private static void drawGlossyCircularBall(Graphics2D g2d, Point center, int radius, Color lightColor, Color darkColor, float transparency)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);
    g2d.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);

    Point gradientOrigin = new Point(center.x - radius,
                                     center.y - radius);
    GradientPaint gradient = new GradientPaint(gradientOrigin.x, 
                                               gradientOrigin.y, 
                                               lightColor, 
                                               gradientOrigin.x, 
                                               gradientOrigin.y + (2 * radius / 3), 
                                               darkColor); // vertical, light on top
    g2d.setPaint(gradient);
    g2d.fillOval((int)(center.x - (radius * 0.90)), 
                 (int)(center.y - (radius * 0.95)), 
                 (int)(2 * radius * 0.9), 
                 (int)(2 * radius * 0.95));
  }

  private void drawGrid(Graphics2D g2d)
  {
    g2d.setColor(Color.gray);
    g2d.setStroke(thick);
    // Center
    g2d.fillOval(center.x - 2, center.y - 2, 4, 4);
    // Equateur celeste
//  g2d.drawOval(center.x - (radius / 2), center.y - (radius / 2), radius, radius);
    // Pole abaiss�
//  g2d.drawOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);
    // Horizontal axis
//  g2d.drawLine(0, center.y, this.getWidth(), center.y);
    
    // Scale, on the edge
    g2d.setStroke(origStroke);
    int externalScaleRadius = (int)(radius * EXTERNAL_RADIUS_COEFF);
    int internalScaleRadius = (int)(radius * INTERNAL_RADIUS_COEFF);
//  g2d.drawOval(center.x - externalScaleRadius, 
//               center.y - externalScaleRadius, 
//               2 * externalScaleRadius, 
//               2 * externalScaleRadius);
    g2d.drawArc(center.x - externalScaleRadius, 
                center.y - externalScaleRadius, 
                2 * externalScaleRadius, 
                2 * externalScaleRadius,
                0,
                180);
//  for (int d=-90; d<=90; d+=1)
    for (double s=0; s<=maxSpeed; s+=increment) 
    {
      double d = speedToAngle(s) - 90;
      int scaleRadius = internalScaleRadius;
      if (s % 1 == 0)
        scaleRadius = externalScaleRadius;
      int fromX = center.x + (int)(radius * Math.sin(Math.toRadians(d - 180)));
      int fromY = center.y + (int)(radius * Math.cos(Math.toRadians(d - 180)));
      int toX   = center.x + (int)(scaleRadius * Math.sin(Math.toRadians(d - 180)));
      int toY   = center.y + (int)(scaleRadius * Math.cos(Math.toRadians(d - 180)));
      g2d.drawLine(fromX, fromY, toX, toY);
      if (s % bigTick == 0) 
      {
        Font f = g2d.getFont();
        float fontFactor = Math.min(this.getWidth(), this.getHeight()) / 20f;
        g2d.setFont(f.deriveFont(Font.BOLD, 2 * fontFactor));
        String str = Integer.toString((int)s);
        int strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(str);
        Color c = g2d.getColor();
        g2d.setColor(Color.white);
        g2d.rotate(Math.toRadians(d), center.x, center.y);
        g2d.drawString(str, center.x - (strWidth / 2), center.y - (int)(0.95 * internalScaleRadius) + (g2d.getFont().getSize() / 2));
        g2d.rotate(Math.toRadians(d) * -1, center.x, center.y);
        g2d.setColor(c);
        g2d.setFont(f);
      }
    }
    if (withMinMax)
    {
      g2d.setColor(Color.red);
      // Min
      double angle = 270 - speedToAngle(this.minimumSpeed);
      // Tip of the triangle
      int toX   = center.x + (int)((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.sin(Math.toRadians(angle)));
      int toY   = center.y + (int)((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.cos(Math.toRadians(angle)));
      
      int toX_1 = center.x + (int)(externalScaleRadius * Math.sin(Math.toRadians(angle + 2)));
      int toY_1 = center.y + (int)(internalScaleRadius * Math.cos(Math.toRadians(angle + 2)));
      
      int toX_2 = center.x + (int)(externalScaleRadius * Math.sin(Math.toRadians(angle - 2)));
      int toY_2 = center.y + (int)(internalScaleRadius * Math.cos(Math.toRadians(angle - 2)));
      
//    g2d.drawLine(center.x, center.y, toX, toY);
      g2d.fillPolygon(new int[] {toX, toX_1, toX_2}, new int[] {toY, toY_1, toY_2}, 3);
      
      // Max
      angle = 270 - speedToAngle(this.maximumSpeed);      
      toX   = center.x + (int)((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.sin(Math.toRadians(angle)));
      toY   = center.y + (int)((radius * INTERNAL_RADIUS_COEFF * 0.8) * Math.cos(Math.toRadians(angle)));
      
      toX_1 = center.x + (int)(externalScaleRadius * Math.sin(Math.toRadians(angle + 2)));
      toY_1 = center.y + (int)(internalScaleRadius * Math.cos(Math.toRadians(angle + 2)));
      
      toX_2 = center.x + (int)(externalScaleRadius * Math.sin(Math.toRadians(angle - 2)));
      toY_2 = center.y + (int)(internalScaleRadius * Math.cos(Math.toRadians(angle - 2)));
      
      //    g2d.drawLine(center.x, center.y, toX, toY);
      g2d.fillPolygon(new int[] {toX, toX_1, toX_2}, new int[] {toY, toY_1, toY_2}, 3);
    }
  }
  
  private double speedToAngle(double s)
  {
    double angle = s * speedUnitRatio;
    return angle + DISPLAY_OFFSET;
  }
  
  private final static DecimalFormat DF = new DecimalFormat("00.0");
  
  private void drawHand(Graphics2D g2d)
  {
    // Display value and unit in a Jumbo
    String value = DF.format(this.speed);
    g2d.setFont(bgJumboFont.deriveFont((radius / 3f)));
    int strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
    g2d.setColor(bgColor);
    g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));
        
    g2d.setFont(jumboFont.deriveFont(Font.BOLD, (radius / 3f)));
    strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
    g2d.setColor(Color.green);
    g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 2));

    // Unit    
    value = speedUnit.label();
    g2d.setFont(bgJumboFont.deriveFont((radius / 6f)));
    strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
    g2d.setColor(bgColor);
    g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));
        
    g2d.setFont(jumboFont.deriveFont((radius / 6f)));
    strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(value);
    g2d.setColor(Color.green);
    g2d.drawString(value, center.x - (strWidth / 2), center.y - (radius / 4));
        
    // Label
    if (this.label.trim().length() > 0)
    {
      g2d.setFont(bgJumboFont.deriveFont((radius / 6f)));
      strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(this.label);
      g2d.setColor(bgColor);
      g2d.drawString(this.label, center.x - (strWidth / 2), this.getHeight() - 2);
          
      g2d.setFont(jumboFont.deriveFont(Font.BOLD, (radius / 6f)));
      strWidth  = g2d.getFontMetrics(g2d.getFont()).stringWidth(this.label);
      g2d.setColor(Color.red);
      g2d.drawString(this.label, center.x - (strWidth / 2), this.getHeight() - 2);      
    }
    
    int longRadius  = (int)(radius * INTERNAL_RADIUS_COEFF * 0.8);
    int shortRadius = (int)(radius * INTERNAL_RADIUS_COEFF * 0.1);
      
    if (this.speed > this.maxSpeed)
      this.speed = this.maxSpeed;
    
    double angle = 270 - speedToAngle(this.speed);
    
    int toX   = center.x + (int)(longRadius * Math.sin(Math.toRadians(angle)));
    int toY   = center.y + (int)(longRadius * Math.cos(Math.toRadians(angle)));

    int fromXRight = center.x + (int)((shortRadius / 2d) * Math.sin(Math.toRadians(angle + 90)));
    int fromYRight = center.y + (int)((shortRadius / 2d) * Math.cos(Math.toRadians(angle + 90)));
      
    int fromXLeft = center.x + (int)((shortRadius / 2d) * Math.sin(Math.toRadians(angle - 90)));
    int fromYLeft = center.y + (int)((shortRadius / 2d) * Math.cos(Math.toRadians(angle - 90)));

    g2d.setColor(Color.black);
    Polygon north = new Polygon(new int[] { toX, fromXRight, fromXLeft }, 
                                new int[] { toY, fromYRight, fromYLeft }, 3);
    g2d.drawPolygon(north);
//  Point gradientOrigin = new Point(toX, toY);
    GradientPaint gradient = new GradientPaint(toX, 
                                               toY, 
                                               Color.cyan, 
                                               center.x, 
                                               center.y, 
                                               Color.blue); // vertical, light on top
    g2d.setPaint(gradient);
//  g2d.setColor(Color.red);
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    g2d.fillPolygon(north);

    drawGlossyCircularBall(g2d, center, (int)(shortRadius * 0.55), Color.lightGray, Color.darkGray, 1f);
  }
}
