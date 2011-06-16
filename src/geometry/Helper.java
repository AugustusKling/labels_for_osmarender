package geometry;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Provides a single instance of a geometry factory to all other classes
 */
public class Helper {

	private static final GeometryFactory geoFactory = new GeometryFactory();

	public static GeometryFactory getGeometryFactory() {
		return geoFactory;
	}

}
