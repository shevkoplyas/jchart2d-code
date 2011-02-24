/*
 *  AErrorBarPolicyConfigurable.java of project jchart2d, a basic 
 *  error bar policy implementation that allows configuration of 
 *  the dimension and direction error bars will be visible in. 
 *  Copyright (c) 2007 Achim Westermann, created on 08.08.2006 08:05:54.
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
package info.monitorenter.gui.chart.errorbars;

import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.TracePoint2D;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.SwingPropertyChangeSupport;

/**
 * A <code>{@link info.monitorenter.gui.chart.IErrorBarPolicy}</code> base
 * implementation that is configurable by the means of showing positive/negative
 * errors in x/y dimension.
 * <p>
 * 
 * Implementations have to implement the methods <br>
 * <code>
 * {@link #internalGetNegativeXError(int, int, TracePoint2D)}<br/>
 * {@link #internalGetNegativeYError(int, int, TracePoint2D)}<br/>
 * {@link #internalGetPositiveXError(int, int, TracePoint2D)}<br/>
 * {@link #internalGetPositiveYError(int, int, TracePoint2D)}<br/>
 * </code>.
 * <p>
 * Please see the class description of
 * {@link info.monitorenter.gui.chart.IErrorBarPixel} for details.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * @version $Revision: 1.26 $
 */
public abstract class AErrorBarPolicyConfigurable implements IErrorBarPolicy,
    PropertyChangeListener {

  /** The internal set of error bar painters delegated to. */
  private Set<IErrorBarPainter> m_errorBarPainters = new LinkedHashSet<IErrorBarPainter>();

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
  protected int m_lastX;

  /**
   * The last trace point coordinate that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics2D)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */

  protected TracePoint2D m_lastPoint;

  /**
   * The last y coordinate that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics2D, TracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics2D)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */
  protected int m_lastY;

  /**
   * The instance that add support for firing <code>PropertyChangeEvents</code>
   * and maintaining <code>PropertyChangeListeners</code>.
   * {@link PropertyChangeListener} instances.
   */
  protected PropertyChangeSupport m_propertyChangeSupport = new SwingPropertyChangeSupport(this);

  /** Internal shared error bar pixel instance to save Object allocation. */
  private ErrorBarPixel m_reusedErrorBarPixel = new ErrorBarPixel(null);

  /** Internal shared error bar value instance to save Object allocation. */
  // private ErrorBarValue m_reusedErrorBarValue;
  /** Flag that controls display of negative errors in x dimension. */
  private boolean m_showNegativeXErrors;

  /** Flag that controls display of negative errors in y dimension. */
  private boolean m_showNegativeYErrors;

  /** Flag that controls display of positive errors in x dimension. */
  private boolean m_showPositiveXErrors;

  /** Flag that controls display of positive errors in y dimension. */
  private boolean m_showPositiveYErrors;

  /** The trace to render. */
  private ITrace2D m_trace;

  /**
   * Defcon.
   * <p>
   */
  public AErrorBarPolicyConfigurable() {
    // this.m_reusedErrorBarValue = new ErrorBarValue();
    // this.m_reusedErrorBarPixel.setValue(this.m_reusedErrorBarValue);
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#addErrorBarPainter(info.monitorenter.gui.chart.IErrorBarPainter)
   */
  public void addErrorBarPainter(final IErrorBarPainter painter) {
    this.m_errorBarPainters.add(painter);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION_COLOR, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT_COLOR, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT_COLOR, this);

    this.firePropertyChange(IErrorBarPolicy.PROPERTY_ERRORBARPAINTER, null, painter);
    this.firePropertyChange(IErrorBarPolicy.PROPERTY_CONFIGURATION, null, null);
  }

  /**
   * Adds a property change listener.
   * <p>
   * 
   * @param propertyName
   *            The name of the property to listen on.
   * @param listener
   *            The PropertyChangeListener to be added.
   * @see info.monitorenter.gui.chart.ITrace2D#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public final void addPropertyChangeListener(final String propertyName,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#calculateErrorBar(int,
   *      int, info.monitorenter.gui.chart.errorbars.ErrorBarPixel,
   *      info.monitorenter.gui.chart.TracePoint2D)
   */
  public final void calculateErrorBar(final int xPixel, final int yPixel,
      final ErrorBarPixel errorBar, final TracePoint2D original) {
    errorBar.clear();

    if (this.m_showNegativeXErrors) {
      errorBar.setNegativeXErrorPixel(this.internalGetNegativeXError(xPixel, yPixel, original));
    }
    if (this.m_showNegativeYErrors) {
      errorBar.setNegativeYErrorPixel(this.internalGetNegativeYError(xPixel, yPixel, original));
    }
    if (this.m_showPositiveXErrors) {
      errorBar.setPositiveXErrorPixel(this.internalGetPositiveXError(xPixel, yPixel, original));
    }
    if (this.m_showPositiveYErrors) {
      errorBar.setPositiveYErrorPixel(this.internalGetPositiveYError(xPixel, yPixel, original));
    }
  }

  /**
   * Compares this instance to the given <code>{@link ITracePainter}</code> by
   * comparing the class name <code>Strings</code>.
   * <p>
   * 
   * @param arg0
   *            the instance to compare with.
   * 
   * @return the result of <code>{@link String#compareTo(String)}</code> with
   *         the class name as value.
   */
  public int compareTo(final ITracePainter arg0) {
    return this.getClass().getName().compareTo(arg0.getClass().getName());
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
    if (g2d != null) {
      this.calculateErrorBar(this.getPreviousX(), this.getPreviousY(), this.m_reusedErrorBarPixel,
          this.getPreviousTracePoint());
      for (IErrorBarPainter painter : this.m_errorBarPainters) {
        painter.paintErrorBar(this.getPreviousX(), this.getPreviousY(), g2d,
            this.m_reusedErrorBarPixel);
      }
    }
  }

  /**
   * Fires a property change event to the registered listeners.
   * <p>
   * 
   * @param property
   *            one of the <code>PROPERTY_XXX</code> constants defined in
   *            <code>{@link info.monitorenter.gui.chart.ITrace2D}</code>.
   * 
   * @param oldvalue
   *            the old value of the property.
   * 
   * @param newvalue
   *            the new value of the property.
   */
  protected final void firePropertyChange(final String property, final Object oldvalue,
      final Object newvalue) {
    this.m_propertyChangeSupport.firePropertyChange(property, oldvalue, newvalue);
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#getCustomConfigurator()
   */
  public JComponent getCustomConfigurator() {
    return null;
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#getErrorBarPainters()
   */
  public Set<IErrorBarPainter> getErrorBarPainters() {
    return this.m_errorBarPainters;
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
  private int getPreviousX() {
    int result = this.m_lastX;
    if (this.m_isEnded) {
      this.m_lastX = Integer.MIN_VALUE;
      if (this.m_lastY == Integer.MIN_VALUE) {
        this.m_isEnded = false;
      }

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
  private TracePoint2D getPreviousTracePoint() {
    TracePoint2D result = this.m_lastPoint;
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
  private int getPreviousY() {
    int result = this.m_lastY;
    if (this.m_isEnded) {
      this.m_lastY = Integer.MIN_VALUE;
      if (this.m_lastX == Integer.MIN_VALUE) {
        this.m_isEnded = false;
      }
    }
    return result;
  }

  /**
   * Returns the trace error bars have to be rendered for.
   * <p>
   * 
   * @return the trace for rendering error bars.
   */
  protected final ITrace2D getTrace() {
    return this.m_trace;
  }

  /**
   * Internally compute the negative x error for the given point as a pixel
   * value (not relative to the the origin value).
   * <p>
   * 
   * @param xPixel
   *            the x value in pixel for the error to render.
   * 
   * @param yPixel
   *            the y value in pixel for the error to render.
   * 
   * @param original
   *            the original point, possibly useful for calculations.
   * 
   * @return the negative x error in pixel for the given point as an absolute
   *         value (not relative to the the origin value).
   * 
   */
  protected abstract int internalGetNegativeXError(final int xPixel, final int yPixel,
      final TracePoint2D original);

  /**
   * Internally compute the negative y error for the given point as a pixel
   * value (not relative to the the origin value).
   * <p>
   * 
   * @param xPixel
   *            the x value in pixel for the error to render.
   * 
   * @param yPixel
   *            the y value in pixel for the error to render.
   * 
   * @return the negative y error in pixel for the given point as an absolute
   *         value (not relative to the the origin value).
   * 
   * @param original
   *            the original point, possibly useful for calculations.
   */
  protected abstract int internalGetNegativeYError(final int xPixel, final int yPixel,
      final TracePoint2D original);

  /**
   * Internally compute the positive x error in pixel for the given point as an
   * absolute value (not relative to the the origin value).
   * <p>
   * 
   * @param xPixel
   *            the x value in pixel for the error to render.
   * 
   * @param yPixel
   *            the y value in pixel for the error to render.
   * 
   * @param original
   *            the original point, possibly useful for calculations.
   * 
   * @return the positive x error in pixel for the given point as an absolute
   *         value (not relative to the the origin value).
   */
  protected abstract int internalGetPositiveXError(final int xPixel, final int yPixel,
      final TracePoint2D original);

  /**
   * Internally compute the positive y error in pixel for the given point as an
   * absolute value (not relative to the the origin value).
   * <p>
   * 
   * @param xPixel
   *            the x coordinate in pixel for the error to render.
   * 
   * @param yPixel
   *            the y coordinate in pixel for the error to render.
   * 
   * @param original
   *            the original point, possibly useful for calculations.
   * 
   * @return the positive y error in pixel for the given point as an absolute
   *         value (not relative to the the origin value).
   */
  protected abstract int internalGetPositiveYError(final int xPixel, final int yPixel,
      final TracePoint2D original);

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#isShowNegativeXErrors()
   */
  public final boolean isShowNegativeXErrors() {
    return this.m_showNegativeXErrors;
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#isShowNegativeYErrors()
   */
  public final boolean isShowNegativeYErrors() {
    return this.m_showNegativeYErrors;
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#isShowPositiveXErrors()
   */
  public final boolean isShowPositiveXErrors() {
    return this.m_showPositiveXErrors;
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#isShowPositiveYErrors()
   */
  public final boolean isShowPositiveYErrors() {
    return this.m_showPositiveYErrors;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
   *      int, java.awt.Graphics2D, info.monitorenter.gui.chart.TracePoint2D)
   */
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics2D g, final TracePoint2D original) {

    this.calculateErrorBar(absoluteX, absoluteY, this.m_reusedErrorBarPixel, original);
    for (IErrorBarPainter painter : this.m_errorBarPainters) {
      painter.paintErrorBar(absoluteX, absoluteY, g, this.m_reusedErrorBarPixel);
    }

    this.m_lastX = nextX;
    this.m_lastY = nextY;
    this.m_lastPoint = original;
  }

  /**
   * Just turns the property change event of subsequent configuration (like
   * <code>{@link IErrorBarPainter#PROPERTY_CONNECTION}</code> to
   * <code>{@link IErrorBarPolicy#PROPERTY_CONFIGURATION}</code> and informs
   * outer <code>{@link PropertyChangeListener}</code> addes with
   * <code>{@link #addPropertyChangeListener(String, PropertyChangeListener)}</code>.
   * <p>
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    this.firePropertyChange(PROPERTY_CONFIGURATION, evt.getOldValue(), evt.getNewValue());
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#removeErrorBarPainter(info.monitorenter.gui.chart.IErrorBarPainter)
   */
  public boolean removeErrorBarPainter(final IErrorBarPainter painter) {
    boolean result = this.m_errorBarPainters.remove(painter);
    painter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION, this);
    painter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION_COLOR, this);
    painter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT, this);
    painter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT_COLOR, this);
    painter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT, this);
    painter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT_COLOR, this);

    if (result) {
      this.firePropertyChange(IErrorBarPolicy.PROPERTY_ERRORBARPAINTER, painter, null);
      this.firePropertyChange(IErrorBarPolicy.PROPERTY_CONFIGURATION, null, null);
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final String property,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.removePropertyChangeListener(property, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#setErrorBarPainter(info.monitorenter.gui.chart.IErrorBarPainter)
   */
  public void setErrorBarPainter(final IErrorBarPainter painter) {
    Iterator<IErrorBarPainter> it = this.m_errorBarPainters.iterator();
    IErrorBarPainter removePainter;
    while (it.hasNext()) {
      removePainter = it.next();
      it.remove();
      removePainter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION, this);
      removePainter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION_COLOR, this);
      removePainter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT, this);
      removePainter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT_COLOR, this);
      removePainter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT, this);
      removePainter.removePropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT_COLOR, this);

      this.firePropertyChange(IErrorBarPolicy.PROPERTY_ERRORBARPAINTER, removePainter, null);

    }
    this.m_errorBarPainters.add(painter);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_CONNECTION_COLOR, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_ENDPOINT_COLOR, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT, this);
    painter.addPropertyChangeListener(IErrorBarPainter.PROPERTY_STARTPOINT_COLOR, this);

    this.firePropertyChange(IErrorBarPolicy.PROPERTY_ERRORBARPAINTER, null, painter);
    this.firePropertyChange(IErrorBarPolicy.PROPERTY_CONFIGURATION, null, null);
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#setShowNegativeXErrors(boolean)
   */
  public final void setShowNegativeXErrors(final boolean showNegativeXErrors) {
    boolean change = this.m_showNegativeXErrors ^ showNegativeXErrors;
    if (change) {
      this.m_showNegativeXErrors = showNegativeXErrors;
      this.firePropertyChange(PROPERTY_CONFIGURATION, null, null);
    }

  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#setShowNegativeYErrors(boolean)
   */
  public final void setShowNegativeYErrors(final boolean showNegativeYErrors) {
    boolean change = this.m_showNegativeYErrors ^ showNegativeYErrors;
    if (change) {
      this.m_showNegativeYErrors = showNegativeYErrors;
      this.firePropertyChange(PROPERTY_CONFIGURATION, null, null);
    }
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#setShowPositiveXErrors(boolean)
   */
  public final void setShowPositiveXErrors(final boolean showPositiveXErrors) {
    boolean change = this.m_showPositiveXErrors ^ showPositiveXErrors;
    if (change) {
      this.m_showPositiveXErrors = showPositiveXErrors;
      this.firePropertyChange(PROPERTY_CONFIGURATION, null, null);
    }
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#setShowPositiveYErrors(boolean)
   */
  public final void setShowPositiveYErrors(final boolean showPositiveYErrors) {
    boolean change = this.m_showPositiveYErrors ^ showPositiveYErrors;
    if (change) {
      this.m_showPositiveYErrors = showPositiveYErrors;
      this.firePropertyChange(PROPERTY_CONFIGURATION, null, null);
    }
  }

  /**
   * Intended for {@link info.monitorenter.gui.chart.traces.ATrace2D} only that
   * will register itself to the instances added to it.
   * <p>
   * This is support for error bar policies that need information about the
   * whole trace (e.g. median value). It has nothing to do with the kind of
   * error bar policy to be used by a trace. See
   * {@link ITrace2D#setErrorBarPolicy(IErrorBarPolicy)} and
   * {@link ITrace2D#addErrorBarPolicy(IErrorBarPolicy)} for this feature
   * instead.
   * <p>
   * 
   * @param trace
   *            the trace error bars are rendered for.
   */
  public void setTrace(final ITrace2D trace) {
    this.m_trace = trace;
    this.m_reusedErrorBarPixel.setTrace(trace);
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#startPaintIteration(java.awt.Graphics2D)
   */
  public void startPaintIteration(final Graphics2D g2d) {
    // nop
  }
}
