    PROGRAM PACKAGE FOR FLINN-ENGDAHL SEISMIC AND GEOGRAPHIC REGIONALIZATION

This directory contains all the programs and files needed to establish the
Flinn-Engdahl seismic and geographic regionalization databases (1995 revision)
on a system as well as some subroutines to access the databases.  All programs
are written in Fortran.
Any questions about this package should be directed to Bruce Presgrave
(e-mail: caracara@neisb.cr.usgs.gov).

Programs used to establish the F-E database on a system:
	1. newidx.for		(This program should be run first)
	2. febndy.for
	3. mnames.for
	4. chrecl.for
	5. mksrtb.for

Applications subprograms (used to access database once it has been installed):
	1. namnum.for
	2. getnam.for
	3. getnum.for
	4. gtsnum.for
	5. getlun.for

Test program (driver to test subroutine namnum):
	1. ggeog.for

ASCII files used to transfer and reconstruct the F-E database:
	1. quadsidx.asc
	2. nesect.asc
	3. nwsect.asc
	4. sesect.asc
	5. swsect.asc
	6. names.asc
	7. seisrdef.asc

ASCII files of lat-lon pairs for F-E region boundaries (for plotting):
	1. srbound.asc    - for the 50 seismic regions
	2. febound.asc    - for the 757 geographic regions
