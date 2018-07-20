package cn.lockyluo.arcgis_test.model;

/**
 * Created by LockyLuo on 2018/7/20.
 */

public class PointBean {

    /**
     * x : 1.2623048642171426E7
     * y : 2638458.6452137646
     * spatialReference : {"wkid":102100,"latestWkid":3857}
     */

    private double x;
    private double y;
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

    public SpatialReferenceBean getSpatialReference() {
        return spatialReference;
    }

    public void setSpatialReference(SpatialReferenceBean spatialReference) {
        this.spatialReference = spatialReference;
    }

    public static class SpatialReferenceBean {
        /**
         * wkid : 102100
         * latestWkid : 3857
         */

        private int wkid;
        private int latestWkid;

        public int getWkid() {
            return wkid;
        }

        public void setWkid(int wkid) {
            this.wkid = wkid;
        }

        public int getLatestWkid() {
            return latestWkid;
        }

        public void setLatestWkid(int latestWkid) {
            this.latestWkid = latestWkid;
        }
    }
}
