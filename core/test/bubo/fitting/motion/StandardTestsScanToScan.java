/*
 * Copyright (c) 2013, Peter Abeles. All Rights Reserved.
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

package bubo.fitting.motion;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.simulation.d2.features.LineSegmentWorld2D;
import bubo.simulation.d2.features.ModelLrf2DBasic;
import bubo.simulation.d2.features.SimStateLrf2D;
import georegression.misc.test.GeometryUnitTest;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.se.Se2_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Standard tests for Lrf2dScanToScan
 *
 * @author Peter Abeles
 */
public abstract class StandardTestsScanToScan {

    private ModelLrf2DBasic model;
    private SimStateLrf2D state;

    protected double angTol = -1;
    protected double tranTol = -1;

    public abstract Lrf2dScanToScan createAlg();

    Lrf2dParam param;

    @Test
    public void standardTestsCCW() {
        param = new Lrf2dParam("Dummy",Math.PI/2,-Math.PI,100,10,0,0);
        allTests();
    }

    @Test
    public void standardTestsCW() {
        param = new Lrf2dParam("Dummy",-Math.PI/2,Math.PI,100,10,0,0);
        allTests();
    }

    private void allTests() {
        setupSimulation();
        checkPerfectNoHint();
        setupSimulation();
        checkPrefectHint();
        setupSimulation();
        setSecondToFirst();
    }

    public void setupSimulation() {
        LineSegmentWorld2D world = new LineSegmentWorld2D();
        // two perpendicular lines so that the position can be uniquely localized
        world.lines.add( new LineSegment2D_F64(-2,4,2,0) );
        world.lines.add( new LineSegment2D_F64(-2,-4,2,0) );

        state = new SimStateLrf2D(param);

        model = new ModelLrf2DBasic();
        model.setWorld(world);

        if( angTol < 0 || tranTol < 0) {
            throw new RuntimeException("angTol and tranTol must be set");
        }
    }

    /**
     * Perfect observations in an easy scenario
     */
    public void checkPerfectNoHint() {
        Lrf2dScanToScan alg = createAlg();
        alg.setSensorParam(state.getSensorParam());

        // observe before any transform is applied
        model.updateSensor(state);

        alg.setReference(state.getRanges());

        // rotate and translate the sensor
        state.getLocalToParent().set(0.12,-0.12,0.05);

        // make an observation in the new position
        model.updateSensor(state);

        alg.setMatch(state.getRanges());

        // find the motion
        assertTrue(alg.process(null));

        // see if it is close enough to the expected value
        Se2_F64 found = alg.getMotion();
        Se2_F64 expected = state.getLocalToParent();

        GeometryUnitTest.assertEquals(expected.getTranslation(),found.getTranslation(),tranTol);
        assertEquals(expected.getYaw(),found.getYaw(),angTol);
        assertTrue(alg.getError()<0.01);
    }

    /**
     * Apply a large transform that should cause it to fail, check that it failed,
     * provide a hint, and see if it works.
     */
    public void checkPrefectHint() {
        Lrf2dScanToScan alg = createAlg();
        alg.setSensorParam(state.getSensorParam());

        // observe before any transform is applied
        model.updateSensor(state);

        alg.setReference(state.getRanges());

        // rotate and translate the sensor, but make the magnitude so great it should
        //  not be able to recover the motion
        state.getLocalToParent().set(-0.5,-0.3,0.6);

        // make an observation in the new position
        model.updateSensor(state);

        alg.setMatch(state.getRanges());

        // It should not be able to find an answer
        if( alg.process(null) ) {
            Se2_F64 found = alg.getMotion();
            Se2_F64 expected = state.getLocalToParent();
            GeometryUnitTest.assertNotEquals(expected,found,tranTol,angTol);
        }

        // now give it the hint
        assertTrue( alg.process(state.getLocalToParent()));

        Se2_F64 found = alg.getMotion();
        Se2_F64 expected = state.getLocalToParent();
        GeometryUnitTest.assertEquals(expected,found,tranTol,angTol);
        assertTrue(alg.getError()<0.01);
    }

    /**
     * Sees if setSecondToFirst() works by only making calls to setSecondScan()
     */
    public void setSecondToFirst() {
        Lrf2dScanToScan alg = createAlg();
        alg.setSensorParam(state.getSensorParam());

        // observe before any transform is applied
        model.updateSensor(state);

        alg.setMatch(state.getRanges());
        // tell it to swap the first and second scan
        alg.setMatchToReference();

        // rotate and translate the sensor
        state.getLocalToParent().set(0.12,-0.12,0.05);

        // make an observation in the new position
        model.updateSensor(state);

        alg.setMatch(state.getRanges());

        // find the motion
        assertTrue(alg.process(null));

        // see if it is close enough to the expected value
        Se2_F64 found = alg.getMotion();
        Se2_F64 expected = state.getLocalToParent();

        GeometryUnitTest.assertEquals(expected.getTranslation(),found.getTranslation(),tranTol);
        assertEquals(expected.getYaw(),found.getYaw(),angTol);
        assertTrue(alg.getError()<0.01);
    }
}