/*
  The TauP Toolkit: Flexible Seismic Travel-Time and Raypath Utilities.
  Copyright (C) 1998-2000 University of South Carolina

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

  The current version can be found at 
  <A HREF="www.seis.sc.edu">http://www.seis.sc.edu</A>

  Bug reports and comments should be directed to 
  H. Philip Crotwell, crotwell@seis.sc.edu or
  Tom Owens, owens@seis.sc.edu

*/

package edu.sc.seis.fissuresUtil.sac;


import java.io.*;
import java.lang.*;
import java.util.*;

/** Class that represents a sac file. All headers are have the same names
  * as within the Sac program. Can read the whole file or just the header
  * as well as write a file.
  * 
  * @version 1.1 Wed Feb  2 20:40:49 GMT 2000
  * @author H. Philip Crotwell
  */
public class SacTimeSeries {
    public float delta = FLOAT_UNDEF;
    public float depmin = FLOAT_UNDEF;
    public float depmax = FLOAT_UNDEF;
    public float scale = FLOAT_UNDEF;
    public float odelta = FLOAT_UNDEF;
    public float b = FLOAT_UNDEF;
    public float e = FLOAT_UNDEF;
    public float o = FLOAT_UNDEF;
    public float a = FLOAT_UNDEF;
    public float fmt = FLOAT_UNDEF;
    public float t0 = FLOAT_UNDEF;
    public float t1 = FLOAT_UNDEF;
    public float t2 = FLOAT_UNDEF;
    public float t3 = FLOAT_UNDEF;
    public float t4 = FLOAT_UNDEF;
    public float t5 = FLOAT_UNDEF;
    public float t6 = FLOAT_UNDEF;
    public float t7 = FLOAT_UNDEF;
    public float t8 = FLOAT_UNDEF;
    public float t9 = FLOAT_UNDEF;
    public float f = FLOAT_UNDEF;
    public float resp0 = FLOAT_UNDEF;
    public float resp1 = FLOAT_UNDEF;
    public float resp2 = FLOAT_UNDEF;
    public float resp3 = FLOAT_UNDEF;
    public float resp4 = FLOAT_UNDEF;
    public float resp5 = FLOAT_UNDEF;
    public float resp6 = FLOAT_UNDEF;
    public float resp7 = FLOAT_UNDEF;
    public float resp8 = FLOAT_UNDEF;
    public float resp9 = FLOAT_UNDEF;
    public float stla = FLOAT_UNDEF;
    public float stlo = FLOAT_UNDEF;
    public float stel = FLOAT_UNDEF;
    public float stdp = FLOAT_UNDEF;
    public float evla = FLOAT_UNDEF;
    public float evlo = FLOAT_UNDEF;
    public float evel = FLOAT_UNDEF;
    public float evdp = FLOAT_UNDEF;
    public float mag = FLOAT_UNDEF;
    public float user0 = FLOAT_UNDEF;
    public float user1 = FLOAT_UNDEF;
    public float user2 = FLOAT_UNDEF;
    public float user3 = FLOAT_UNDEF;
    public float user4 = FLOAT_UNDEF;
    public float user5 = FLOAT_UNDEF;
    public float user6 = FLOAT_UNDEF;
    public float user7 = FLOAT_UNDEF;
    public float user8 = FLOAT_UNDEF;
    public float user9 = FLOAT_UNDEF;
    public float dist = FLOAT_UNDEF;
    public float az = FLOAT_UNDEF;
    public float baz = FLOAT_UNDEF;
    public float gcarc = FLOAT_UNDEF;
    public float sb = FLOAT_UNDEF;
    public float sdelta = FLOAT_UNDEF;
    public float depmen = FLOAT_UNDEF;
    public float cmpaz = FLOAT_UNDEF;
    public float cmpinc = FLOAT_UNDEF;
    public float xminimum = FLOAT_UNDEF;
    public float xmaximum = FLOAT_UNDEF;
    public float yminimum = FLOAT_UNDEF;
    public float ymaximum = FLOAT_UNDEF;
    public float unused6 = FLOAT_UNDEF;
    public float unused7 = FLOAT_UNDEF;
    public float unused8 = FLOAT_UNDEF;
    public float unused9 = FLOAT_UNDEF;
    public float unused10 = FLOAT_UNDEF;
    public float unused11 = FLOAT_UNDEF;
    public float unused12 = FLOAT_UNDEF;
    public int nzyear = INT_UNDEF;
    public int nzjday = INT_UNDEF;
    public int nzhour = INT_UNDEF;
    public int nzmin = INT_UNDEF;
    public int nzsec = INT_UNDEF;
    public int nzmsec = INT_UNDEF;
    public int nvhdr = 6;
    public int norid = INT_UNDEF;
    public int nevid = INT_UNDEF;
    public int npts = INT_UNDEF;
    public int nsnpts = INT_UNDEF;
    public int nwfid = INT_UNDEF;
    public int nxsize = INT_UNDEF;
    public int nysize = INT_UNDEF;
    public int unused15 = INT_UNDEF;
    public int iftype = INT_UNDEF;
    public int idep = INT_UNDEF;
    public int iztype = INT_UNDEF;
    public int unused16 = INT_UNDEF;
    public int iinst = INT_UNDEF;
    public int istreg = INT_UNDEF;
    public int ievreg = INT_UNDEF;
    public int ievtyp = INT_UNDEF;
    public int iqual = INT_UNDEF;
    public int isynth = INT_UNDEF;
    public int imagtyp = INT_UNDEF;
    public int imagsrc = INT_UNDEF;
    public int unused19 = INT_UNDEF;
    public int unused20 = INT_UNDEF;
    public int unused21 = INT_UNDEF;
    public int unused22 = INT_UNDEF;
    public int unused23 = INT_UNDEF;
    public int unused24 = INT_UNDEF;
    public int unused25 = INT_UNDEF;
    public int unused26 = INT_UNDEF;
    public int leven = INT_UNDEF;
    public int lpspol = INT_UNDEF;
    public int lovrok = INT_UNDEF;
    public int lcalda = INT_UNDEF;
    public int unused27 = INT_UNDEF;
    public String kstnm = STRING8_UNDEF;
    public String kevnm = STRING16_UNDEF;
    public String khole = STRING8_UNDEF;
    public String ko = STRING8_UNDEF;
    public String ka = STRING8_UNDEF;  
    public String kt0 = STRING8_UNDEF;
    public String kt1 = STRING8_UNDEF;
    public String kt2 = STRING8_UNDEF; 
    public String kt3 = STRING8_UNDEF;
    public String kt4 = STRING8_UNDEF;
    public String kt5 = STRING8_UNDEF; 
    public String kt6 = STRING8_UNDEF;
    public String kt7 = STRING8_UNDEF;
    public String kt8 = STRING8_UNDEF; 
    public String kt9 = STRING8_UNDEF;
    public String kf = STRING8_UNDEF;
    public String kuser0 = STRING8_UNDEF;
    public String kuser1 = STRING8_UNDEF;
    public String kuser2 = STRING8_UNDEF;
    public String kcmpnm = STRING8_UNDEF;
    public String knetwk = STRING8_UNDEF;
    public String kdatrd = STRING8_UNDEF;
    public String kinst = STRING8_UNDEF;

    public float[] y;
    public float[] x;
    public float[] real;
    public float[] imaginary;
    public float[] amp;
    public float[] phase;

    // undef values for sac
    public static float FLOAT_UNDEF = -12345.0f;
    public static int INT_UNDEF = -12345;
    public static String STRING8_UNDEF = "-12345  ";
    public static String STRING16_UNDEF = "-12345          "; 

    /* TRUE and FLASE defined for convenience. */
    public static final int TRUE   =  1;
    public static final int FALSE  =  0;

    /* Constants used by sac. */
    public static final int IREAL  =  0; 
    public static final int ITIME  =  1; 
    public static final int IRLIM  =  2; 
    public static final int IAMPH  =  3; 
    public static final int IXY    =  4; 
    public static final int IUNKN  =  5; 
    public static final int IDISP  =  6; 
    public static final int IVEL   =  7; 
    public static final int IACC   =  8; 
    public static final int IB     =  9; 
    public static final int IDAY   = 10; 
    public static final int IO     = 11; 
    public static final int IA     = 12; 
    public static final int IT0    = 13; 
    public static final int IT1    = 14; 
    public static final int IT2    = 15; 
    public static final int IT3    = 16; 
    public static final int IT4    = 17; 
    public static final int IT5    = 18; 
    public static final int IT6    = 19; 
    public static final int IT7    = 20; 
    public static final int IT8    = 21; 
    public static final int IT9    = 22; 
    public static final int IRADNV = 23; 
    public static final int ITANNV = 24; 
    public static final int IRADEV = 25; 
    public static final int ITANEV = 26; 
    public static final int INORTH = 27; 
    public static final int IEAST  = 28; 
    public static final int IHORZA = 29; 
    public static final int IDOWN  = 30; 
    public static final int IUP    = 31; 
    public static final int ILLLBB = 32; 
    public static final int IWWSN1 = 33; 
    public static final int IWWSN2 = 34; 
    public static final int IHGLP  = 35; 
    public static final int ISRO   = 36; 
    public static final int INUCL  = 37; 
    public static final int IPREN  = 38; 
    public static final int IPOSTN = 39; 
    public static final int IQUAKE = 40; 
    public static final int IPREQ  = 41; 
    public static final int IPOSTQ = 42; 
    public static final int ICHEM  = 43; 
    public static final int IOTHER = 44; 
    public static final int IGOOD  = 45; 
    public static final int IGLCH  = 46; 
    public static final int IDROP  = 47; 
    public static final int ILOWSN = 48; 
    public static final int IRLDTA = 49; 
    public static final int IVOLTS = 50; 
    public static final int INIV51 = 51; 
    public static final int INIV52 = 52; 
    public static final int INIV53 = 53; 
    public static final int INIV54 = 54; 
    public static final int INIV55 = 55; 
    public static final int INIV56 = 56; 
    public static final int INIV57 = 57; 
    public static final int INIV58 = 58; 
    public static final int INIV59 = 59; 
    public static final int INIV60 = 60; 
    
    
    public static final int  data_offset = 632;
    
    /** reads the sac file specified by the filename. Only a very simple
     * check is made
     * to be sure the file really is a sac file.
     * @throws FileNotFoundException if the file cannot be found
     * @throws IOException if it isn't a sac file or if it happens :)
     */
    public void read(String filename) 
	throws FileNotFoundException, IOException {
	File sacFile = new File(filename);
	FileInputStream fis = new FileInputStream(filename);
	BufferedInputStream buf = new BufferedInputStream(fis);
	DataInputStream dis = new DataInputStream( buf);
        readHeader(dis);
	if (sacFile.length() != npts*4+data_offset) {
	    throw new IOException(filename+" does not appear to be a sac file!");
	}
	readData(dis);
	dis.close();
    }

    public void read(DataInputStream dis) 
	throws IOException {
	readHeader(dis);
	readData(dis);
    }

    /** reads just the sac header specified by the filename. No checks are
     * made to be sure the file really is a sac file.
     */
    public void readHeader(String filename) 
	throws FileNotFoundException, IOException {
	DataInputStream dis = new DataInputStream(
						  new BufferedInputStream(
									  new FileInputStream(filename)));
	readHeader(dis);
	dis.close();
    }

    /** reads the header from the given stream. */
    public void readHeader(DataInputStream dis) 
	throws FileNotFoundException, IOException {
	delta = dis.readFloat();
	depmin = dis.readFloat();
	depmax = dis.readFloat();
	scale = dis.readFloat();
	odelta = dis.readFloat();
      
	b = dis.readFloat();
	e = dis.readFloat();
	o = dis.readFloat();
	a = dis.readFloat();
	fmt = dis.readFloat();
      
	t0 = dis.readFloat();
	t1 = dis.readFloat();
	t2 = dis.readFloat();
	t3 = dis.readFloat();
	t4 = dis.readFloat();
      
	t5 = dis.readFloat();
	t6 = dis.readFloat();
	t7 = dis.readFloat();
	t8 = dis.readFloat();
	t9 = dis.readFloat();
      
	f = dis.readFloat();
	resp0 = dis.readFloat();
	resp1 = dis.readFloat();
	resp2 = dis.readFloat();
	resp3 = dis.readFloat();
      
	resp4 = dis.readFloat();
	resp5 = dis.readFloat();
	resp6 = dis.readFloat();
	resp7 = dis.readFloat();
	resp8 = dis.readFloat();
      
	resp9 = dis.readFloat();
	stla = dis.readFloat();
	stlo = dis.readFloat();
	stel = dis.readFloat();
	stdp = dis.readFloat();
      
	evla = dis.readFloat();
	evlo = dis.readFloat();
	evel = dis.readFloat();
	evdp = dis.readFloat();
	mag = dis.readFloat();
      
	user0 = dis.readFloat();
	user1 = dis.readFloat();
	user2 = dis.readFloat();
	user3 = dis.readFloat();
	user4 = dis.readFloat();
      
	user5 = dis.readFloat();
	user6 = dis.readFloat();
	user7 = dis.readFloat();
	user8 = dis.readFloat();
	user9 = dis.readFloat();
      
	dist = dis.readFloat();
	az = dis.readFloat();
	baz = dis.readFloat();
	gcarc = dis.readFloat();
	sb = dis.readFloat();
      
	sdelta = dis.readFloat();
	depmen = dis.readFloat();
	cmpaz = dis.readFloat();
	cmpinc = dis.readFloat();
	xminimum = dis.readFloat();
      
	xmaximum = dis.readFloat();
	yminimum = dis.readFloat();
	ymaximum = dis.readFloat();
	unused6 = dis.readFloat();
	unused7 = dis.readFloat();
      
	unused8 = dis.readFloat();
	unused9 = dis.readFloat();
	unused10 = dis.readFloat();
	unused11 = dis.readFloat();
	unused12 = dis.readFloat();
      
	nzyear = dis.readInt();
	nzjday = dis.readInt();
	nzhour = dis.readInt();
	nzmin = dis.readInt();
	nzsec = dis.readInt();
      
	nzmsec = dis.readInt();
	nvhdr = dis.readInt();
	norid = dis.readInt();
	nevid = dis.readInt();
	npts = dis.readInt();
      
	nsnpts = dis.readInt();
	nwfid = dis.readInt();
	nxsize = dis.readInt();
	nysize = dis.readInt();
	unused15 = dis.readInt();
      
	iftype = dis.readInt();
	idep = dis.readInt();
	iztype = dis.readInt();
	unused16 = dis.readInt();
	iinst = dis.readInt();
      
	istreg = dis.readInt();
	ievreg = dis.readInt();
	ievtyp = dis.readInt();
	iqual = dis.readInt();
	isynth = dis.readInt();
      
	imagtyp = dis.readInt();
	imagsrc = dis.readInt();
	unused19 = dis.readInt();
	unused20 = dis.readInt();
	unused21 = dis.readInt();
      
	unused22 = dis.readInt();
	unused23 = dis.readInt();
	unused24 = dis.readInt();
	unused25 = dis.readInt();
	unused26 = dis.readInt();
      
	leven = dis.readInt();
	lpspol = dis.readInt();
	lovrok = dis.readInt();
	lcalda = dis.readInt();
	unused27 = dis.readInt();
      
	byte[] eightBytes = new byte[8];
	byte[] sixteenBytes = new byte[16];

	dis.readFully(eightBytes);   kstnm = new String(eightBytes);
	dis.readFully(sixteenBytes); kevnm = new String(sixteenBytes);
      
	dis.readFully(eightBytes);   khole = new String(eightBytes);
	dis.readFully(eightBytes);   ko = new String(eightBytes);
	dis.readFully(eightBytes);   ka = new String(eightBytes);
      
	dis.readFully(eightBytes);   kt0 = new String(eightBytes);
	dis.readFully(eightBytes);   kt1 = new String(eightBytes);
	dis.readFully(eightBytes);   kt2 = new String(eightBytes);
      
	dis.readFully(eightBytes);   kt3 = new String(eightBytes);
	dis.readFully(eightBytes);   kt4 = new String(eightBytes);
	dis.readFully(eightBytes);   kt5 = new String(eightBytes);
      
	dis.readFully(eightBytes);   kt6 = new String(eightBytes);
	dis.readFully(eightBytes);   kt7 = new String(eightBytes);
	dis.readFully(eightBytes);   kt8 = new String(eightBytes);
      
	dis.readFully(eightBytes);   kt9 = new String(eightBytes);
	dis.readFully(eightBytes);   kf = new String(eightBytes);
	dis.readFully(eightBytes);   kuser0 = new String(eightBytes);
      
	dis.readFully(eightBytes);   kuser1 = new String(eightBytes);
	dis.readFully(eightBytes);   kuser2 = new String(eightBytes);
	dis.readFully(eightBytes);   kcmpnm = new String(eightBytes);
      
	dis.readFully(eightBytes);   knetwk = new String(eightBytes);
	dis.readFully(eightBytes);   kdatrd = new String(eightBytes);
	dis.readFully(eightBytes);   kinst = new String(eightBytes);
    }

    /** read the data portion of the given File */
    public void readData(DataInputStream fis) throws IOException {
	InputStream in = fis;

	y = new float[npts];

	int numAdded = 0;
	int numRead;
	int i;
        byte[] overflow = new byte[4];
        byte[] prevoverflow = new byte[4];
        int overflowBytes=0;
        int prevoverflowBytes=0;
	byte[] buf = new byte[4096];  // buf length must be == 0 % 4
	// and for efficiency, should be 
	// a multiple of the disk sector size
	while (numAdded < npts) {
            if ((numRead = in.read(buf)) == 0) {
                continue;
            } else if (numRead == -1) {
                // EOF
                throw new EOFException();
            }
            overflowBytes = (numRead+prevoverflowBytes) % 4;
	    if (overflowBytes != 0) {
		// partial read of bytes for last value
                // save in overflow
                System.arraycopy(buf, numRead - overflowBytes, 
                                 overflow, 0, 
                                 overflowBytes);
	    }
	    i=0;
            if (prevoverflowBytes != 0) {
                int temp = 0;
                // use leftover bytes
                for(i=0;i<prevoverflowBytes; i++) {
                    temp <<= 8;
                    temp += (prevoverflow[i] & 0xff);
                }
                // use first new bytes as needed
                for(i=0;i<4-prevoverflowBytes; i++) {
                    temp <<= 8;
                    temp += (buf[i] & 0xff);
                }
                y[numAdded] = Float.intBitsToFloat(temp);
                numAdded++;
            }
            // i is now set to first unused byte in buf
	    while ( i<= numRead-4) {
		y[numAdded++] = 
                    Float.intBitsToFloat( ((buf[i++] & 0xff) << 24) + 
                                          ((buf[i++] & 0xff) << 16) + 
                                          ((buf[i++] & 0xff) << 8) + 
                                          ((buf[i++] & 0xff) << 0));
	    }
            System.arraycopy(overflow, 0, 
                             prevoverflow, 0, 
                             overflowBytes);
            prevoverflowBytes = overflowBytes;
	}
    }

    /** reads the data portion from the given stream. 
     *  Uses readFloat repeatedly resulting in MUCH slower read
     *  times than the slightly more confusing method above. */
    protected void readDataOld(DataInputStream dis)
	throws FileNotFoundException, IOException {

	y = new float[npts];
	for (int i=0; i<npts; i++) {
	    y[i] = dis.readFloat();
	}


	if (leven == SacTimeSeries.FALSE ||
	    iftype == SacTimeSeries.IRLIM ||
	    iftype == SacTimeSeries.IAMPH) {
	    x = new float[npts];
	    for (int i=0; i<npts; i++) {
		x[i] = dis.readFloat();
	    }
	    if (iftype == SacTimeSeries.IRLIM) {
		real = y;
		imaginary = x;
	    }
	    if (iftype == SacTimeSeries.IAMPH) {
		amp = y;
		phase = x;
	    }
	}
    }

    /** writes this object out as a sac file. */
    public void write(String filename) 
	throws FileNotFoundException, IOException {
	File f = new File(filename);
	write(f);
    }

    /** writes this object out as a sac file. */
    public void write(File file) 
	throws FileNotFoundException, IOException {	
	DataOutputStream dos = new DataOutputStream(
						    new BufferedOutputStream(
									     new FileOutputStream(file)));
        writeHeader(dos);
        writeData(dos);
	dos.close();
    }

    public void writeHeader(DataOutputStream dos) 
	throws IOException {
	dos.writeFloat(delta);
	dos.writeFloat(depmin);
	dos.writeFloat(depmax);
	dos.writeFloat(scale);
	dos.writeFloat(odelta);
 
	dos.writeFloat(b);
	dos.writeFloat(e);
	dos.writeFloat(o);
	dos.writeFloat(a);
	dos.writeFloat(fmt);
 
	dos.writeFloat(t0);
	dos.writeFloat(t1);
	dos.writeFloat(t2);
	dos.writeFloat(t3);
	dos.writeFloat(t4);

	dos.writeFloat(t5);
	dos.writeFloat(t6);
	dos.writeFloat(t7);
	dos.writeFloat(t8);
	dos.writeFloat(t9);

	dos.writeFloat(f);
	dos.writeFloat(resp0);
	dos.writeFloat(resp1);
	dos.writeFloat(resp2);
	dos.writeFloat(resp3);

	dos.writeFloat(resp4);
	dos.writeFloat(resp5);
	dos.writeFloat(resp6);
	dos.writeFloat(resp7);
	dos.writeFloat(resp8);

	dos.writeFloat(resp9);
	dos.writeFloat(stla);
	dos.writeFloat(stlo);
	dos.writeFloat(stel);
	dos.writeFloat(stdp);
      
	dos.writeFloat(evla);
	dos.writeFloat(evlo);
	dos.writeFloat(evel);
	dos.writeFloat(evdp);
	dos.writeFloat(mag);
      
	dos.writeFloat(user0);
	dos.writeFloat(user1);
	dos.writeFloat(user2);
	dos.writeFloat(user3);
	dos.writeFloat(user4);
      
	dos.writeFloat(user5);
	dos.writeFloat(user6);
	dos.writeFloat(user7);
	dos.writeFloat(user8);
	dos.writeFloat(user9);
      
	dos.writeFloat(dist);
	dos.writeFloat(az);
	dos.writeFloat(baz);
	dos.writeFloat(gcarc);
	dos.writeFloat(sb);
      
	dos.writeFloat(sdelta);
	dos.writeFloat(depmen);
	dos.writeFloat(cmpaz);
	dos.writeFloat(cmpinc);
	dos.writeFloat(xminimum);
      
	dos.writeFloat(xmaximum);
	dos.writeFloat(yminimum);
	dos.writeFloat(ymaximum);
	dos.writeFloat(unused6);
	dos.writeFloat(unused7);
      
	dos.writeFloat(unused8);
	dos.writeFloat(unused9);
	dos.writeFloat(unused10);
	dos.writeFloat(unused11);
	dos.writeFloat(unused12);
      
	dos.writeInt(nzyear);
	dos.writeInt(nzjday);
	dos.writeInt(nzhour);
	dos.writeInt(nzmin);
	dos.writeInt(nzsec);
      
	dos.writeInt(nzmsec);
	dos.writeInt(nvhdr);
	dos.writeInt(norid);
	dos.writeInt(nevid);
	dos.writeInt(npts);
      
	dos.writeInt(nsnpts);
	dos.writeInt(nwfid);
	dos.writeInt(nxsize);
	dos.writeInt(nysize);
	dos.writeInt(unused15);
      
	dos.writeInt(iftype);
	dos.writeInt(idep);
	dos.writeInt(iztype);
	dos.writeInt(unused16);
	dos.writeInt(iinst);
      
	dos.writeInt(istreg);
	dos.writeInt(ievreg);
	dos.writeInt(ievtyp);
	dos.writeInt(iqual);
	dos.writeInt(isynth);
      
	dos.writeInt(imagtyp);
	dos.writeInt(imagsrc);
	dos.writeInt(unused19);
	dos.writeInt(unused20);
	dos.writeInt(unused21);
      
	dos.writeInt(unused22);
	dos.writeInt(unused23);
	dos.writeInt(unused24);
	dos.writeInt(unused25);
	dos.writeInt(unused26);
      
	dos.writeInt(leven);
	dos.writeInt(lpspol);
	dos.writeInt(lovrok);
	dos.writeInt(lcalda);
	dos.writeInt(unused27);
      
	if (kstnm.length() > 8) {kstnm = kstnm.substring(0,7); }
	while (kstnm.length() < 8) {kstnm += " "; }
	dos.writeBytes(kstnm);
	if (kevnm.length() > 16) {kevnm = kevnm.substring(0,15); }
	while (kevnm.length() < 16) { kevnm += " "; }
	dos.writeBytes(kevnm);
      
	if (khole.length() > 8) {khole = khole.substring(0,7); }
	while (khole.length() < 8) {khole += " "; }
	dos.writeBytes(khole);
	if (ko.length() > 8) {ko = ko.substring(0,7); }
	while (ko.length() < 8) {ko += " "; }
	dos.writeBytes(ko);
	if (ka.length() > 8) {ka = ka.substring(0,7); }
	while (ka.length() < 8) {ka += " "; }
	dos.writeBytes(ka);
      
	if (kt0.length() > 8) {kt0 = kt0.substring(0,7); }
	while (kt0.length() < 8) {kt0 += " "; }
	dos.writeBytes(kt0);
	if (kt1.length() > 8) {kt1 = kt1.substring(0,7); }
	while (kt1.length() < 8) {kt1 += " "; }
	dos.writeBytes(kt1);
	if (kt2.length() > 8) {kt2 = kt2.substring(0,7); }
	while (kt2.length() < 8) {kt2 += " "; }
	dos.writeBytes(kt2);
      
	if (kt3.length() > 8) {kt3 = kt3.substring(0,7); }
	while (kt3.length() < 8) {kt3 += " "; }
	dos.writeBytes(kt3);
	if (kt4.length() > 8) {kt4 = kt4.substring(0,7); }
	while (kt4.length() < 8) {kt4 += " "; }
	dos.writeBytes(kt4);
	if (kt5.length() > 8) {kt5 = kt5.substring(0,7); }
	while (kt5.length() < 8) {kt5 += " "; }
	dos.writeBytes(kt5);
      
	if (kt6.length() > 8) {kt6 = kt6.substring(0,7); }
	while (kt6.length() < 8) {kt6 += " "; }
	dos.writeBytes(kt6);
	if (kt7.length() > 8) {kt7 = kt7.substring(0,7); }
	while (kt7.length() < 8) {kt7 += " "; }
	dos.writeBytes(kt7);
	if (kt8.length() > 8) {kt8 = kt8.substring(0,7); }
	while (kt8.length() < 8) {kt8 += " "; }
	dos.writeBytes(kt8);
      
	if (kt9.length() > 8) {kt9 = kt9.substring(0,7); }
	while (kt9.length() < 8) {kt9 += " "; }
	dos.writeBytes(kt9);
	if (kf.length() > 8) {kf = kf.substring(0,7); }
	while (kf.length() < 8) {kf += " "; }
	dos.writeBytes(kf);
	if (kuser0.length() > 8) {kuser0 = kuser0.substring(0,7); }
	while (kuser0.length() < 8) {kuser0 += " "; }
	dos.writeBytes(kuser0);
      
	if (kuser1.length() > 8) {kuser1 = kuser1.substring(0,7); }
	while (kuser1.length() < 8) {kuser1 += " "; }
	dos.writeBytes(kuser1);
	if (kuser2.length() > 8) {kuser2 = kuser2.substring(0,7); }
	while (kuser2.length() < 8) {kuser2 += " "; }
	dos.writeBytes(kuser2);
	if (kcmpnm.length() > 8) {kcmpnm = kcmpnm.substring(0,7); }
	while (kcmpnm.length() < 8) {kcmpnm += " "; }
	dos.writeBytes(kcmpnm);
      
	if (knetwk.length() > 8) {knetwk = knetwk.substring(0,7); }
	while (knetwk.length() < 8) {knetwk += " "; }
	dos.writeBytes(knetwk);
	if (kdatrd.length() > 8) {kdatrd = kdatrd.substring(0,7); }
	while (kdatrd.length() < 8) {kdatrd += " "; }
	dos.writeBytes(kdatrd);
	if (kinst.length() > 8) {kinst = kinst.substring(0,7); }
	while (kinst.length() < 8) {kinst += " "; }
	dos.writeBytes(kinst);

    }

    public void writeData(DataOutputStream dos)  
	throws IOException {

	for (int i=0; i<npts; i++) {
	    dos.writeFloat(y[i]);
	}
 
 
	if (leven == SacTimeSeries.FALSE ||
	    iftype == SacTimeSeries.IRLIM ||
	    iftype == SacTimeSeries.IAMPH) {
	    for (int i=0; i<npts; i++) {
		dos.writeFloat(x[i]);
	    }
	}
    }

    public void printHeader() {
	System.out.println("delta = "+delta+ 
	" depmin = "+depmin+ 
	" depmax = "+depmax+ 
	" scale = "+scale+ 
	" odelta = "+odelta);
	
	System.out.println("b = "+b+ 
	" e = "+e+ 
	" o = "+o+ 
	" a = "+a+ 
	" fmt = "+fmt);
      
	System.out.println("t0 = "+t0+ 
	" t1 = "+t1+ 
	" t2 = "+t2+ 
	" t3 = "+t3+ 
	" t4 = "+t4);
      
	System.out.println("t5 = "+t5+ 
	" t6 = "+t6+ 
	" t7 = "+t7+ 
	" t8 = "+t8+ 
	" t9 = "+t9);
      
	System.out.println("f = "+f+ 
	" resp0 = "+resp0+ 
	" resp1 = "+resp1+ 
	" resp2 = "+resp2+ 
	" resp3 = "+resp3);
      
	System.out.println("resp4 = "+resp4+ 
	" resp5 = "+resp5+ 
	" resp6 = "+resp6+ 
	" resp7 = "+resp7+ 
	" resp8 = "+resp8);
      
	System.out.println("resp9 = "+resp9+ 
	" stla = "+stla+ 
	" stlo = "+stlo+ 
	" stel = "+stel+ 
	" stdp = "+stdp);
      
	System.out.println("evla = "+evla+ 
	" evlo = "+evlo+ 
	" evel = "+evel+ 
	" evdp = "+evdp+ 
	" mag = "+mag);
      
	System.out.println("user0 = "+user0+ 
	" user1 = "+user1+ 
	" user2 = "+user2+ 
	" user3 = "+user3+ 
	" user4 = "+user4);
      
	System.out.println("user5 = "+user5+ 
	" user6 = "+user6+ 
	" user7 = "+user7+ 
	" user8 = "+user8+ 
	" user9 = "+user9);
      
	System.out.println("dist = "+dist+ 
	" az = "+az+ 
	" baz = "+baz+ 
	" gcarc = "+gcarc+ 
	" sb = "+sb);
      
	System.out.println("sdelta = "+sdelta+ 
	" depmen = "+depmen+ 
	" cmpaz = "+cmpaz+ 
	" cmpinc = "+cmpinc+ 
	" xminimum = "+xminimum);
      
	System.out.println("xmaximum = "+xmaximum+ 
	" yminimum = "+yminimum+ 
	" ymaximum = "+ymaximum+ 
	" unused6 = "+unused6+ 
	" unused7 = "+unused7);
      
	System.out.println("unused8 = "+unused8+ 
	" unused9 = "+unused9+ 
	" unused10 = "+unused10+ 
	" unused11 = "+unused11+ 
	" unused12 = "+unused12);
      
	System.out.println("nzyear = "+nzyear+ 
	" nzjday = "+nzjday+ 
	" nzhour = "+nzhour+ 
	" nzmin = "+nzmin+ 
	" nzsec = "+nzsec);
      
	System.out.println("nzmsec = "+nzmsec+ 
	" nvhdr = "+nvhdr+ 
	" norid = "+norid+ 
	" nevid = "+nevid+ 
	" npts = "+npts);
      
	System.out.println("nsnpts = "+nsnpts+ 
	" nwfid = "+nwfid+ 
	" nxsize = "+nxsize+ 
	" nysize = "+nysize+ 
	" unused15 = "+unused15);
      
	System.out.println("iftype = "+iftype+ 
	" idep = "+idep+ 
	" iztype = "+iztype+ 
	" unused16 = "+unused16+ 
	" iinst = "+iinst);
      
	System.out.println("istreg = "+istreg+ 
	" ievreg = "+ievreg+ 
	" ievtyp = "+ievtyp+ 
	" iqual = "+iqual+ 
	" isynth = "+isynth);
      
	System.out.println("imagtyp = "+imagtyp+ 
	" imagsrc = "+imagsrc+ 
	" unused19 = "+unused19+ 
	" unused20 = "+unused20+ 
	" unused21 = "+unused21);
      
	System.out.println("unused22 = "+unused22+ 
	" unused23 = "+unused23+ 
	" unused24 = "+unused24+ 
	" unused25 = "+unused25+ 
	" unused26 = "+unused26);
      
	System.out.println("leven = "+leven+ 
	" lpspol = "+lpspol+ 
	" lovrok = "+lovrok+ 
	" lcalda = "+lcalda+ 
	" unused27 = "+unused27);

	System.out.println(
	" kstnm = "+kstnm+
	" kevnm = "+kevnm+
	" khole = "+khole+
	" ko = "+ko);

	System.out.println(
	" ka = "+ka+
	" kt0 = "+kt0+
	" kt1 = "+kt1+
	" kt2 = "+kt2);
	System.out.println(
	" kt3 = "+kt3+
	" kt4 = "+kt4+
	" kt5 = "+kt5+
	" kt6 = "+kt6);
	System.out.println(
	" kt7 = "+kt7+
	" kt8 = "+kt8+
	" kt9 = "+kt9+

	" kf = "+kf);
	System.out.println(
	" kuser0 = "+kuser0+
	" kuser1 = "+kuser1+
	" kuser2 = "+kuser2+
	" kcmpnm = "+kcmpnm);
	System.out.println(
	" knetwk = "+knetwk+
	" kdatrd = "+kdatrd+
	" kinst = "+kinst);
    }
      
    /** just for testing. Reads the filename given as the argument,
     *  writes out some header variables and then
     *  writes it back out as "outsacfile".
     */
    public static void main(String[] args) {
	SacTimeSeries data = new SacTimeSeries();
       
	if (args.length != 1) {
	    System.out.println("Usage: java SacTimeSeries sacsourcefile ");
	    return;
	}
       
	try {
	   
	    data.read(args[0]);
	    //    data.y = new float[100000];
	    // 	   for (int i=0; i<100000; i++) {
	    // 	       data.y[i] = (float)Math.sin(Math.PI*i/18000)/1000000.0f;
	    // 	       data.y[i] = (float)Math.sin(Math.PI*i/18000);
	    // 	       //System.out.println("point is " + data.y[i]);
	    // 	   }
	    // 	   data.npts = data.y.length;

            data.printHeader();
	    data.write("outsacfile");
	    System.out.println("Done writing");
	} catch (FileNotFoundException e) {
	    System.out.println("File "+args[0]+" doesn't exist.");
	} catch (IOException e) {
	    System.out.println("IOException: "+e.getMessage());
	}
    }
}
