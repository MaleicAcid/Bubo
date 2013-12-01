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

package bubo.filters.ekf;

import bubo.filters.MultivariateGaussianDM;
import bubo.filters.abst.KalmanFilterInterface;
import bubo.filters.kf.DKFCommon;
import org.ejml.data.DenseMatrix64F;

import static org.ejml.ops.CommonOps.subEquals;


/**
 * This is an implementation of a first order continuous discrete extended Kalman filter.
 * The implementation below is the same as the implementation provided in:
 * <p/>
 * "Estimation with Applications to Tracking and Navigation" by Bar-Shalom, et all.
 * <p/>
 * The version provided in "Applied Optimal Estimation"  edited by Arthur Gelb is slightly
 * different in the covariance propagation step.  I haven't bothered to step through the derivations
 * to decide which one is better.  The implementation provided here seems to be more
 * common in recent literature.
 * <p/>
 * kinematics model:
 * <p/>
 * x_k = f(x_{k-1}) + w_k
 * z_k = h(x_k) + v_k
 * <p/>
 * w_k ~ N(0,Q_k)
 * v_k ~ N(0,R_k)
 * <p/>
 * This implementation assumes a that all the measurements it gets have the same dimension.
 */
public class ExtendedKalmanFilter extends DKFCommon implements KalmanFilterInterface {
	// describes how the state changes as a function of time
	private EkfPredictorDiscrete predictor;

	// the state to meas matrix
	private EkfProjector projector;

	public ExtendedKalmanFilter(EkfPredictorDiscrete predictor, EkfProjector projector) {
		super(predictor.getSystemSize(), projector.getMeasurementSize());

		this.predictor = predictor;
		this.projector = projector;
	}

	/**
	 * This constructor should be called when the predictor and projectors will be
	 * changed dynamically.
	 *
	 * @param stateDOF number of degrees of freedom in the state
	 * @param measDOF  number of degrees of freedom in the measurement
	 */
	public ExtendedKalmanFilter(int stateDOF, int measDOF) {
		super(stateDOF, measDOF);

		this.predictor = null;
		this.projector = null;
	}

	/**
	 * Used to change the propagator used by the filter.
	 */
	public void setPredictor(EkfPredictorDiscrete predictor) {
		if (predictor.getSystemSize() != getStateDOF())
			throw new IllegalArgumentException("The predictor must have the same DOF as the filter");

		this.predictor = predictor;
	}

	public void setProjector(EkfProjector projector) {
		if (projector.getMeasurementSize() != getMeasDOF())
			throw new IllegalArgumentException("The projector must have the same DOF as the filter");

		this.projector = projector;
	}

	public <T extends EkfPredictor> T getPredictor() {
		return (T) predictor;
	}

	public EkfProjector getProjector() {
		return projector;
	}

	/**
	 * This will predict the state of the system forward in time by the specified time step.
	 * <p/>
	 * The results of the prediction are stored in the 'state' variable.
	 */
	@Override
	public void predict(MultivariateGaussianDM state) {
		predictor.compute(state.getMean());

		DenseMatrix64F F = predictor.getJacobianF();
		DenseMatrix64F Q = predictor.getPlantNoise();

		DenseMatrix64F x = state.getMean();
		DenseMatrix64F P = state.getCovariance();

		// update the state estimate
		x.set(predictor.getPredictedState());

		_predictCovariance(F, Q, P);
	}

	/**
	 * Updates the state estimate using the provided measurement
	 */
	@Override
	public void update(MultivariateGaussianDM state, MultivariateGaussianDM meas) {
		projector.compute(state.getMean());
		DenseMatrix64F H = projector.getJacobianH();

		DenseMatrix64F x = state.getMean();
		DenseMatrix64F P = state.getCovariance();

		DenseMatrix64F z = meas.getMean();
		DenseMatrix64F R = meas.getCovariance();

		// compute the residual
		y.set(z);
		subEquals(y, projector.getProjected());

		_updateCovariance(H, x, P, R);
	}
}
