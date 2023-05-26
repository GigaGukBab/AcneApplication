package com.example.acneapplication.NaverMapsFiles;

public class CoordinateConverter {
    private static final double RE = 6371.00877; // 지구 반경(km)
    private static final double GRID = 5.0; // 격자 간격(km)
    private static final double SLAT1 = 30.0; // 투영 위도1(degree)
    private static final double SLAT2 = 60.0; // 투영 위도2(degree)
    private static final double OLON = 126.0; // 기준점 경도(degree)
    private static final double OLAT = 38.0; // 기준점 위도(degree)
    private static final double XO = 43; // 기준점 X좌표(GRID)
    private static final double YO = 136; // 기1준점 Y좌표(GRID)

    // LCC DFS 좌표변환 (도분초 to 경위도)
    public double[] convertGRID_GPS(double lat, double lon) {
        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        double rs[] = new double[2];
        rs[0] = lat;
        rs[1] = lon;
        double ra = Math.tan(Math.PI * 0.25 + (lat) / 2.0 * DEGRAD);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon - OLON;
        theta *= DEGRAD;
        theta = Math.atan2(Math.sin(theta), Math.cos(theta));
        double rx = Math.sin(theta) * ra / ro + XO + 0.5;
        double ry = YO + 0.5 - Math.cos(theta) * ra / ro;
        rs[0] = ry;
        rs[1] = rx;
        return rs;
    }
}
