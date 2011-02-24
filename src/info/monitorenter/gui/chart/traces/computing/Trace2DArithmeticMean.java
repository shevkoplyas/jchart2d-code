/*
 *  Trace2DArithmeticMean.java of project jchart2d, a trace 
 *  that accumulates the latest n points added to a single point with the arithmetic 
 *  mean value. 
 *  Copyright 2007 (C) Achim Westermann, created on 23:59:21.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart.traces.computing;

import info.monitorenter.gui.chart.TracePoint2D;
import info.monitorenter.gui.chart.traces.ATrace2D;
import info.monitorenter.util.collections.IRingBuffer;
import info.monitorenter.util.collections.RingBufferArrayFast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A trace that accumulates the lates n points added to a single point with the
 * arithmetic mean value.
 * <p>
 * 
 * Please note that this trace scan be used in two modes:
 * <ol>
 * <li> Stand alone: <br/> Add the <code>ITrace2D</code> implementation to a
 * chart and add data points to it as normal. </li>
 * <li> Computing trace: <br/> Add the <code>ITrace2D</code> implementation as
 * a computing trace to an existing trace via
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#addComputingTrace(info.monitorenter.gui.chart.ITrace2D)}</code>
 * and only add data points to the original trace. Add the computing trace to
 * the same chart and updates of the original trace will be reflected on the
 * computing trace as well. </li>
 * </ol>
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 7.0.0
 * 
 */
public class Trace2DArithmeticMean
    extends ATrace2D {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -4365986306182830082L;

  /** The buffer for the points about to be merged. */
  private IRingBuffer<TracePoint2D> m_pointBuffer;

  /** The internal list of points to render. */
  private List<TracePoint2D> m_points = new LinkedList<TracePoint2D>();

  /**
   * The amount of n recent points to buffer. private int m_pointBufferSize; /**
   * Constructor with the given amount of points to merge into one point with
   * their arithmetic mean.
   * <p>
   * 
   * @param arithmenticMeanSpan
   *            the amount of points to merge into one point with their
   *            arithmetic mean.
   */
  public Trace2DArithmeticMean(final int arithmenticMeanSpan) {
    super();
    this.m_pointBuffer = new RingBufferArrayFast<TracePoint2D>(arithmenticMeanSpan);
  }

  /**
   * @see info.monitorenter.gui.chart.traces.ATrace2D#addPointInternal(info.monitorenter.gui.chart.TracePoint2D)
   */
  protected boolean addPointInternal(final TracePoint2D p) {
    this.m_pointBuffer.add(p);
    TracePoint2D cumulate = this.getArithmeticMean();
    boolean result = this.m_points.add(cumulate);
    this.firePointAdded(cumulate);
    return result;
  }

  /**
   * Returns a point with the arithmetic mean values for x and y computed of the
   * last n added points (n was constructor - given).
   * <p>
   * 
   * @return a point with the arithmetic mean values for x and y computed of the
   *         last n added points (n was constructor - given).
   */
  private TracePoint2D getArithmeticMean() {
    double x = 0;
    double y = 0;
    TracePoint2D result;
    for (TracePoint2D point : this.m_pointBuffer) {
      result = point;
      x += result.getX();
      y += result.getY();
    }
    int divisor = this.m_pointBuffer.size();
    if (divisor == 0) {
      divisor = 1;
    }
    y /= divisor;
    x /= divisor;
    result = new TracePoint2D(x, y);
    return result;

  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMaxSize()
   */
  public int getMaxSize() {
    return Integer.MAX_VALUE;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getSize()
   */
  public int getSize() {

    return this.m_points.size();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#isEmpty()
   */
  public boolean isEmpty() {
    return this.m_points.isEmpty();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#iterator()
   */
  public Iterator<TracePoint2D> iterator() {
    return this.m_points.iterator();
  }

  /**
   * @see info.monitorenter.gui.chart.traces.ATrace2D#removeAllPointsInternal()
   */
  protected void removeAllPointsInternal() {
    this.m_pointBuffer.clear();
    this.m_points.clear();

  }

  /**
   * @see info.monitorenter.gui.chart.traces.ATrace2D#removePointInternal(info.monitorenter.gui.chart.TracePoint2D)
   */
  protected TracePoint2D removePointInternal(final TracePoint2D point) {
    TracePoint2D result = this.m_points.remove(0);
    return result;
  }

}
