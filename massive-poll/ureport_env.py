import os,sys
from django.core.management import setup_environ

sys.path.insert(0,os.getcwd()) 

import perf_settings
 
setup_environ(perf_settings)

import djcelery
djcelery.setup_loader()

print "\nInitialised ureport env [" + str(__name__) + "] from  : [" + os.getcwd() + "]"
