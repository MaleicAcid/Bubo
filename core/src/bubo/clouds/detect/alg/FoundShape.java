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

package bubo.clouds.detect.alg;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure which contains information on found shapes from {@link PointCloudShapeDetectionSchnabel2007}.
 *
 * @author Peter Abeles
 */
public class FoundShape {
	/**
	 * Model parameters of the shape
	 */
	public Object modelParam;
	/**
	 * Which shape it matched.  Index in the model list provided in the constructor of
	 * {@link PointCloudShapeDetectionSchnabel2007}
	 */
	public int whichShape;
	/**
	 * Points which matched the shape
	 */
	public List<PointVectorNN> points = new ArrayList<PointVectorNN>();
}
