#!/bin/bash

VIRTUALENV_ACTIVATE=${UREPORT_VIRTUAL_ENV_HOME}/bin/activate



PYTHONPATH="`pwd`"

echo "PYTHONPATH=${PYTHONPATH}"
echo "Going to create a massive poll..."

cd ${UREPORT_HOME}

bash -c "source ${VIRTUALENV_ACTIVATE} && ./ci-start-celery.sh celery_test_settings"   

cd ureport_project

bash -c "source ${VIRTUALENV_ACTIVATE} && ./manage.py runscript create_massive_poll --verbosity=1 --settings=perf_settings"   

cd ..

bash -c "source ${VIRTUALENV_ACTIVATE} && ./ci-stop-celery.sh celery_test_settings"   

echo "Poll created."

cd -
