package cn.lockyluo.arcgis_test.model;

/**
 * Created by LockyLuo on 2018/7/18.
 */

public class Geo {

    /**
     * rotation : 360
     * scale : 65535.99999998646
     * targetGeometry : {"x":113.393676,"y":23.050644000000002,"z":9.313225746154785E-10,"spatialReference":{"wkid":4326}}
     */

    private double rotation;
    private double scale=0;
    private TargetGeometryBean targetGeometry;

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public TargetGeometryBean getTargetGeometry() {
        return targetGeometry;
    }

    public void setTargetGeometry(TargetGeometryBean targetGeometry) {
        this.targetGeometry = targetGeometry;
    }

    public static class TargetGeometryBean {
        /**
         * x : 113.393676
         * y : 23.050644000000002
         * z : 9.313225746154785E-10
         * spatialReference : {"wkid":4326}
         */

        private double x=113.393676;
        private double y=23.050644;
        private double z=0;
        private SpatialReferenceBean spatialReference;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public SpatialReferenceBean getSpatialReference() {
            return spatialReference;
        }

        public void setSpatialReference(SpatialReferenceBean spatialReference) {
            this.spatialReference = spatialReference;
        }

        public static class SpatialReferenceBean {
            /**
             * wkid : 4326
             */

            private int wkid;

            public int getWkid() {
                return wkid;
            }

            public void setWkid(int wkid) {
                this.wkid = wkid;
            }
        }
    }
}
