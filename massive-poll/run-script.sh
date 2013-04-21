#!/bin/bash

VIRTUALENV_ACTIVATE="${UREPORT_VIRTUAL_ENV_HOME}/bin/activate"

SCRIPT_PATH="`pwd`"
SCRIPT_NAME=$1


echo "Going to run a script..."


cd ${UREPORT_HOME}

bash -c "source ${VIRTUALENV_ACTIVATE} && ./ci-start-celery.sh perf_settings"  

sleep 2 

cd ureport_project

bash -c "source ${VIRTUALENV_ACTIVATE} && ${SCRIPT_PATH}/${SCRIPT_NAME}"   


cd ..

bash -c "source ${VIRTUALENV_ACTIVATE} && ./ci-stop-celery.sh perf_settings"   

cd -

echo "Script completed."
