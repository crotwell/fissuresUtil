package edu.sc.seis.fissuresUtil.bag;

/**
c
c Subroutine to calculate the Great Circle Arc distance
c    between two sets of geographic coordinates
c
c Given:  stalat => Latitude of first point (+N, -S) in degrees
c         stalon => Longitude of first point (+E, -W) in degrees
c         evtlat => Latitude of second point
c         evtlon => Longitude of second point
c
c Returns:  delta => Great Circle Arc distance in degrees
c           az    => Azimuth from pt. 1 to pt. 2 in degrees
c           baz   => Back Azimuth from pt. 2 to pt. 1 in degrees
c
c If you are calculating station-epicenter pairs, pt. 1 is the station
c
c Equations take from Bullen, pages 154, 155
c
c T. Owens, September 19, 1991
c           Sept. 25 -- fixed az and baz calculations
c
  P. Crotwell, Setember 27, 1995
            Converted to c to fix annoying problem of fortran giving wrong
               answers if the input doesn't contain a decimal point.

  H. P. Crotwell, September 18, 1997
            Java version for direct use in java programs.
*/
public class DistAz {

   public double delta;

   public double az;

   public double baz;

   public DistAz(double stalat, double stalon, double evtlat, double evtlon){
      if ((stalat == evtlat)&&(stalon == evtlon)) {
         delta = 0.0;
         az = 0.0;
         baz = 0.0;
         return;
      }
      double scolat, slon, ecolat, elon;
      double a,b,c,d,e,aa,bb,cc,dd,ee,g,gg,h,hh,k,kk;
      double rhs1,rhs2,sph,rad,del,daz,dbaz;
 
      rad=2.*Math.PI/360.0;
/*
c
c scolat and ecolat are the geocentric colatitudes
c as defined by Richter (pg. 318)
c
c Earth Flattening of 1/298.257 take from Bott (pg. 3)
c
*/
      sph=1.0/298.257;
 
      scolat=Math.PI/2.0 - Math.atan((1.-sph)*(1.-sph)*Math.tan(stalat*rad));
      ecolat=Math.PI/2.0 - Math.atan((1.-sph)*(1.-sph)*Math.tan(evtlat*rad));
      slon=stalon*rad;
      elon=evtlon*rad;
/*
c
c  a - e are as defined by Bullen (pg. 154, Sec 10.2)
c     These are defined for the pt. 1
c
*/
      a=Math.sin(scolat)*Math.cos(slon);
      b=Math.sin(scolat)*Math.sin(slon);
      c=Math.cos(scolat);
      d=Math.sin(slon);
      e=-Math.cos(slon);
      g=-c*e;
      h=c*d;
      k=-Math.sin(scolat);
/*
c
c  aa - ee are the same as a - e, except for pt. 2
c
*/
      aa=Math.sin(ecolat)*Math.cos(elon);
      bb=Math.sin(ecolat)*Math.sin(elon);
      cc=Math.cos(ecolat);
      dd=Math.sin(elon);
      ee=-Math.cos(elon);
      gg=-cc*ee;
      hh=cc*dd;
      kk=-Math.sin(ecolat);
/*
c
c  Bullen, Sec 10.2, eqn. 4
c
*/
      del=Math.acos(a*aa + b*bb + c*cc);
      delta=del/rad;
/*
c
c  Bullen, Sec 10.2, eqn 7 / eqn 8
c
c    pt. 1 is unprimed, so this is technically the baz
c
c  Calculate baz this way to avoid quadrant problems
c
*/
      rhs1=(aa-d)*(aa-d)+(bb-e)*(bb-e)+cc*cc - 2.;
      rhs2=(aa-g)*(aa-g)+(bb-h)*(bb-h)+(cc-k)*(cc-k) - 2.;
      dbaz=Math.atan2(rhs1,rhs2);
      if (dbaz<0.0) {
         dbaz=dbaz+2*Math.PI;
      }
      baz=dbaz/rad;
/*
c
c  Bullen, Sec 10.2, eqn 7 / eqn 8
c
c    pt. 2 is unprimed, so this is technically the az
c
*/
      rhs1=(a-dd)*(a-dd)+(b-ee)*(b-ee)+c*c - 2.;
      rhs2=(a-gg)*(a-gg)+(b-hh)*(b-hh)+(c-kk)*(c-kk) - 2.;
      daz=Math.atan2(rhs1,rhs2);
      if(daz<0.0) {
         daz=daz+2*Math.PI;
      }
      az=daz/rad;
/*
c
c   Make sure 0.0 is always 0.0, not 360.
c
*/
      if(Math.abs(baz-360.) < .00001) baz=0.0;
      if(Math.abs(az-360.) < .00001) az=0.0;
 
   }

   public static void main(String[] args) {
      if (args.length != 4) {
       System.out.println("Usage: java DistAz sta_lat sta_lon evt_lat evt_lon");
         System.out.println("       Returns:  Delta Baz Az");
         System.exit(1);
      }

      double stalat = Double.valueOf(args[0]).doubleValue();
      double stalon = Double.valueOf(args[1]).doubleValue();
      double evtlat = Double.valueOf(args[2]).doubleValue();
      double evtlon = Double.valueOf(args[3]).doubleValue();

      DistAz distaz = new DistAz(stalat, stalon, evtlat, evtlon);
      System.out.println("   dist="+distaz.delta+"   baz="+distaz.baz+
         "   az="+distaz.az);
   }
}
