/*
 * ATracePainter.java,  base class for ITracePainter implementations.
 * Copyright (c) 2007  Achim Westermann, Achim.Westermann@gmx.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 */
package info.monitorenter.gui.chart.traces.painters;

import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.TracePoint2D;

import java.awt.Graphics2D;

/**
 * A trace painter that adds the service of knowing the previous point that had
 * to be painted.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.14 $
 * 
 */
public abstract class ATracePainter implements info.monitorenter.gui.chart.ITracePainter {

  /** Flag to remember if a paint iteration has ended. */
  private boolean m_isEnded = false;

  /**
   * The last x coordinate that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics2D)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */
  private int m_previousX;

  /**
   * The last y coordinate that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics2D)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */
  private int m_previousY;

  /**
   * The last trace point that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics2D)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */
  private TracePoint2D m_previousPoint;

  /**
   * @param o
   *            the instance to compare to.
   * 
   * @return see interface.
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(final ITracePainter o) {

    return this.getClass().getName().compareTo(o.getClass().getName());
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#discontinue(java.awt.Graphics2D)
   */
  public void discontinue(final Graphics2D g2d) {
    this.endPaintIteration(g2d);
    this.startPaintIteration(g2d);
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#endPaintIteration(java.awt.Graphics2D)
   */
  public void endPaintIteration(final Graphics2D g2d) {
    // nop
  }

  /**
   * Two instances are judged equal if they are of the same class.
   * <p>
   * This implies that any state of a {@link ATracePainter} is unimportant -
   * implementations that have a state (e.g. the radius for discs to paint in
   * {@link TracePainterDisc}) this method should be considered to be
   * overrriden (along with {@link #hashCode()}.
   * <p>
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(final Object obj) {
    boolean result = false;
    if (obj != null) {
      result = (this.getClass() == obj.getClass());
    }
    return result;
  }

  /**
   * Returns the previous X value that had to be painted by
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * 
   * This value will be {@link Integer#MIN_VALUE} if no previous point had to be
   * painted.
   * <p>
   * 
   * @return the previous X value that had to be painted by
   *         {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   */
  public int getPreviousX() {
    int result = this.m_previousX;
    if (this.m_isEnded) {
      this.m_previousX = Integer.MIN_VALUE;
      if (this.m_previousY == Integer.MIN_VALUE) {
        this.m_isEnded = false;
      }

    }
    return result;
  }

  /**
   * Returns the previous Y value that had to be painted by
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * 
   * This value will be {@link Integer#MIN_VALUE} if no previous point had to be
   * painted.
   * <p>
   * 
   * @return the previous Y value that had to be painted by
   *         {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   */
  public int getPreviousY() {
    int result = this.m_previousY;
    if (this.m_isEnded) {
      this.m_previousY = Integer.MIN_VALUE;
      if (this.m_previousX == Integer.MIN_VALUE) {
        this.m_isEnded = false;
      }
    }
    return result;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return this.getClass().hashCode();
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
   *      int, java.awt.Graphics2D, info.monitorenter.gui.chart.TracePoint2D)
   */
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics2D g, final TracePoint2D original) {
    this.m_previousX = nextX;
    this.m_previousY = nextY;
    this.m_previousPoint = original;
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#startPaintIteration(java.awt.Graphics2D)
   */
  public void startPaintIteration(final Graphics2D g2d) {
    // nop
  }

  /**
   * Returns the previous trace point that had to be painted by
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * 
   * This value will be <code>null</code> if no previous point had to be
   * painted.
   * <p>
   * 
   * @return the previous trace point that had to be painted by
   *         {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   */
  protected TracePoint2D getPreviousPoint() {
    return this.m_previousPoint;
  }

}
