/*
 * Copyright (c) 2013-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Project BUBO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bubo.simulation;

import boofcv.gui.image.ShowImages;
import bubo.gui.maps.MapDisplay;
import bubo.io.maps.MapIO;
import bubo.maps.d2.lines.LineSegmentMap;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.point.Point2D_F64;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Application for creating a map made out line segments.  Used to define walls which the
 * robot can't pass through.
 *
 * @author Peter Abeles
 */
public class CreateLineMapApp extends MapDisplay
		implements MouseListener , KeyListener {

	boolean hasFirst = false;
	Point2D_F64 first = new Point2D_F64();
	Point2D_F64 previous = new Point2D_F64();

	public CreateLineMapApp() {
		setMapWalls(new LineSegmentMap());
		addMouseListener(this);
		addKeyListener(this);
		grabFocus();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if( e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
			System.out.println("saving map to walls.csv");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MapIO.save(getMapWalls(), "walls.csv");
				}
			});
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if( !hasFirst ) {
			hasFirst = true;
			imageToMap(e.getX(), e.getY(), first);
			previous.set(first);
		} else if( e.getButton() == 2 ) {
			hasFirst = false;
		} else if( e.getButton() == 3 ) {
			LineSegment2D_F64 line = new LineSegment2D_F64();
			line.a.set(previous);
			line.b.set(first);
			addToMap(line);
			hasFirst = false;
		} else {
			LineSegment2D_F64 line = new LineSegment2D_F64();
			line.a.set(previous);
			imageToMap(e.getX(),e.getY(),line.b);
			addToMap(line);
			previous.set(line.b);
		}
	}

	public void addToMap( final LineSegment2D_F64 line ) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getMapWalls().getLines().add(line);
				repaint();
			}
		});
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	public static void main(String[] args) {
		CreateLineMapApp app = new CreateLineMapApp();
		app.setPreferredSize(new Dimension(500,500));
		JFrame frame = ShowImages.showWindow(app,"Map Maker");
		frame.addKeyListener(app);
	}
}
