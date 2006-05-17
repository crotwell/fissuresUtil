#!/bin/sh
export SNEP=/data/SNEP
export DHI=$SNEP/DHI
populate(){
    echo $1
    cd $DHI/db
    ./bin/startDb
    cd $DHI/importer
    time ./bin/PopulateDatabaseFromDirectory $SNEP/Tar_Extracts/$1
    cp DatabasePopulationReport.txt ${1}Report.txt
    cp DatabasePopulationReport.pdf ${1}Report.pdf
    cd ..
    tar czf $1.tar.gz importer
    cd $DHI/db
    kill `cat *pid`
    sleep 2
    tar czf $1.tar.gz db
}

populate August_Service_2005
populate First_Check_2005
populate January_Service_2006
populate October_Service_2005
