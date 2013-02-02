package cp.overlays;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PathOverlay extends Overlay {

    private ArrayList<GeoPoint> pointList;
    //private int color; //swapnil.c
    private int colorPath, colorTurn;
    
    //swapnil.c:start
    private String direction, latitudeTurn, longitudeTurn;
    private String[] directionInfo;
    private GeoPoint gpback, gpforward, gpcenter;    
    private Double latitude, longitude;
    private GeoPoint turnPt;
    private int isTurn;
    //swapnil.c:end

    public PathOverlay(ArrayList<GeoPoint> pointList, int color) {
            this.pointList = pointList;     
            this.colorPath = color;
            this.isTurn = 0;
    }

    public PathOverlay(ArrayList<GeoPoint> pointList, int colorPath, String turnList[], int colorTurn) {
        this.pointList = pointList;      	
        this.colorPath = colorPath;
        this.directionInfo = turnList;     
        this.colorTurn = colorTurn;
        this.isTurn = 1;
    }
    
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	        Point current = new Point();
	        Path path = new Path();
	        int count=0;
	        Projection projection = mapView.getProjection();
	        Iterator<GeoPoint> iterator = pointList.iterator();
	        if (iterator.hasNext()) {
	            projection.toPixels(iterator.next(), current);
	            path.moveTo((float) current.x, (float) current.y);
	        } else return;
	        while(iterator.hasNext()) {
	            projection.toPixels(iterator.next(), current);
	            path.lineTo((float) current.x, (float) current.y);
	        }
	
	        Paint pathPaint = new Paint();
	        pathPaint.setAntiAlias(true);
	        pathPaint.setColor(colorPath);
	        pathPaint.setStyle(Style.STROKE);
			pathPaint.setStrokeJoin(Paint.Join.ROUND);
			pathPaint.setStrokeCap(Paint.Cap.ROUND);
			pathPaint.setStrokeWidth(15);
	        canvas.drawPath(path, pathPaint);

	        
        //for getting turn points //swapnil.c start
	     if(isTurn==1){
			 //Configuring the paint brush
			 Paint mPaint = new Paint();
			 mPaint.setDither(true);
			 mPaint.setColor(colorTurn);
			 mPaint.setStyle(Paint.Style.STROKE);
			 mPaint.setStrokeJoin(Paint.Join.ROUND);
			 mPaint.setStrokeCap(Paint.Cap.ROUND);
			 mPaint.setStrokeWidth(15);
			 
		
				int length = directionInfo.length;
				int indexTurnPt;
				double r, thetaSlope;
				Point h1 = new Point(0,0); //forming x- axis
				Point h2 = new Point(10,0); //forming x- axis
				Path ph = new Path();
				double angleArrow = (Math.PI/6.0);
								
	
				for(int i = 0; i < length; i+=5){
					gpcenter = null;
					gpforward = null;
					gpback = null;
					
					direction = directionInfo[i];
					longitudeTurn = directionInfo[i+2].toString();
					latitudeTurn = directionInfo[i+3].toString();
					
					longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
					latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
					turnPt = new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());
									
					
					indexTurnPt = pointList.indexOf(turnPt);
					
					if(indexTurnPt<0) //swapnil.c : skip that turn point as not in direction list
						continue;
					
					gpcenter = pointList.get(indexTurnPt);
					if(indexTurnPt!=0)
						gpback = pointList.get(indexTurnPt-1);
					if(indexTurnPt+1 != pointList.size())
						gpforward = pointList.get(indexTurnPt+1); 
					
					
					Point p1 = new Point(); //start_line
					Point p2 = new Point(); //turnPt_intersection
					Point p3 = new Point(); //end_arrow
					Path path1 = new Path(); //line
					Path path2 = new Path(); //arrow
					
					Point p4 = new Point(); //arrowIncLeft
					Point p5 = new Point(); //arrowIncRight
					Point p6 = new Point(); //arrowIncCenter
					
					Path path3 = new Path(); //arrowIncLeft
					Path path4 = new Path(); //arrowIncRight
					
					
					//direction arrow
					if(gpback != null)
						projection.toPixels(gpback, p1);
					if(gpcenter != null)
						projection.toPixels(gpcenter, p2);
					if(gpforward != null)
						projection.toPixels(gpforward, p3);
					
					//arrow center
					p6.x = (p2.x+p3.x)/2;
					p6.y = (p2.y+p3.y)/2;
					
					if(gpback != null && gpcenter != null) {
						path1.moveTo(p2.x, p2.y); //Moving to center
						path1.lineTo((p1.x+2*p2.x)/3, (p1.y+2*p2.y)/3); //line till intersection
					}
					if(gpforward != null && gpcenter != null) {
						path2.moveTo(p2.x, p2.y); //move to intersection
						path2.lineTo((p2.x+p3.x)/2, (p2.y+p3.y)/2); //arrow
					}
					
						
						r = (Math.sqrt(Math.pow(p6.x-p2.x, 2.0)+Math.pow(p6.y-p2.y, 2.0)))/3; // 1/4 of length of line
						
						if((p6.x-p2.x) != 0)
							thetaSlope = Math.atan(((double)(p6.y-p2.y)*1.0)/((double)(p6.x-p2.x)*1.0));
						else
							if(p6.y > p2.y)
								thetaSlope = (Math.PI)/2; //pi/2
							else
								thetaSlope = (Math.PI)*1.5; //3pi/2
						
						if(p6.x < p2.x) //going left 
							thetaSlope += Math.PI;
						
						p4.x = (int) (p6.x - r*(Math.cos(thetaSlope - angleArrow)));
						p4.y = (int) (p6.y - r*(Math.sin(thetaSlope - angleArrow)));
						
						p5.x = (int) (p6.x - r*(Math.cos(thetaSlope + angleArrow)));
						p5.y = (int) (p6.y - r*(Math.sin(thetaSlope + angleArrow)));
						
						path3.moveTo(p6.x, p6.y);
						path3.lineTo(p4.x, p4.y);
						
						path4.moveTo(p6.x, p6.y);
						path4.lineTo(p5.x, p5.y);
						
						canvas.drawPath(path3, mPaint);
						canvas.drawPath(path4, mPaint);
						canvas.drawPath(path1, mPaint);
						canvas.drawPath(path2, mPaint);
			    }
	     }
        //for getting turn points //swapnil.c end
    }
}