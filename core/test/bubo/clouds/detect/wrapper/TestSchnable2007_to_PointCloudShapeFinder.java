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

package bubo.clouds.detect.wrapper;

import bubo.clouds.FactoryPointCloudShape;
import bubo.clouds.detect.PointCloudShapeFinder;
import bubo.clouds.detect.alg.ConfigSchnabel2007;

/**
 * @author Peter Abeles
 */
public class TestSchnable2007_to_PointCloudShapeFinder extends GeneralChecksPointCloudShapeFinder {

	public TestSchnable2007_to_PointCloudShapeFinder() {
		super(10, 1e-6);
	}

	@Override
	public PointCloudShapeFinder createAlgorithm() {

		ConfigSchnabel2007 configRansac = ConfigSchnabel2007.createDefault(100, 0.5, 0.1);
		configRansac.minModelAccept = 50;
		configRansac.octreeSplit = 60;

		ConfigSurfaceNormals configSurface = new ConfigSurfaceNormals(6, 20, 3);
		ConfigRemoveFalseShapes configMerge = new ConfigRemoveFalseShapes(0.7);

		return FactoryPointCloudShape.ransacOctree(configSurface, configRansac, configMerge);
	}
}